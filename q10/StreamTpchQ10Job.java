package org.example.q10;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;


import org.example.q10.model.*;

import java.time.LocalDate;

public class StreamTpchQ10Job {


    private static final OutputTag<String> TAG_LI = new OutputTag<String>("LI"){};
    private static final OutputTag<String> TAG_OR = new OutputTag<String>("OR"){};
    private static final OutputTag<String> TAG_CU = new OutputTag<String>("CU"){};
    private static final OutputTag<String> TAG_NA = new OutputTag<String>("NA"){};

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: <inputFile> <outputFile> <startDate(yyyy-MM-dd)>");
            return;
        }
        final String input   = args[0];
        final String output  = args[1];
        final LocalDate lower= LocalDate.parse(args[2]);
        final LocalDate upper= lower.plusMonths(3);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        SingleOutputStreamOperator<String> head = env.readTextFile(input)
                .process(new ProcessFunction<String, String>() {
                    @Override public void processElement(String v, Context ctx, Collector<String> out){
                        if (v.length() < 3) return;
                        String tag  = v.substring(1,3);
                        String body = v.charAt(0) + v.substring(3);
                        switch(tag){
                            case "LI": ctx.output(TAG_LI, body); break;
                            case "OR": ctx.output(TAG_OR, body); break;
                            case "CU": ctx.output(TAG_CU, body); break;
                            case "NA": ctx.output(TAG_NA, body); break;
                        }
                    }
                });


        DataStream<LineItemRec> li = head.getSideOutput(TAG_LI).map(toLineItem());
        DataStream<OrderRec>    or = head.getSideOutput(TAG_OR).map(toOrder());
        DataStream<CustomerRec> cu = head.getSideOutput(TAG_CU).map(toCustomer());
        DataStream<NationRec>   na = head.getSideOutput(TAG_NA).map(toNation());


        li = li.filter(LineItemRec::isReturned);
        or = or.filter(o -> !o.getOrderDate().isBefore(lower) && o.getOrderDate().isBefore(upper));


        DataStream<JoinLineItemOrderRec> liOr = li.keyBy(LineItemRec::getOrderKey)
                .connect(or.keyBy(OrderRec::getOrderKey))
                .process(new CoProcessFunction<LineItemRec, OrderRec, JoinLineItemOrderRec>(){
                    private transient MapState<Integer, LineItemRec> items; // key=lineNumber
                    private transient ValueState<OrderRec> order;
                    @Override public void open(Configuration c){
                        items = getRuntimeContext().getMapState(new MapStateDescriptor<>("li", Integer.class, LineItemRec.class));
                        order = getRuntimeContext().getState(new ValueStateDescriptor<>("or", OrderRec.class));
                    }
                    @Override public void processElement1(LineItemRec li, Context ctx, Collector<JoinLineItemOrderRec> out) throws Exception {
                        if (li.isInsert()) {
                            items.put(li.getLineNumber(), li);
                            OrderRec o = order.value();
                            if (o != null) emit(out, true, li, o);
                        } else {
                            items.remove(li.getLineNumber());
                            OrderRec o = order.value();
                            if (o != null) emit(out, false, li, o);
                        }
                    }
                    @Override public void processElement2(OrderRec o, Context ctx, Collector<JoinLineItemOrderRec> out) throws Exception {
                        if (o.isInsert()) {
                            order.update(o);
                            for (LineItemRec li : items.values()) emit(out, true, li, o);
                        } else {
                            OrderRec prev = order.value();
                            order.clear();
                            for (LineItemRec li : items.values()) emit(out, false, li, prev==null?o:prev);
                        }
                    }
                    private void emit(Collector<JoinLineItemOrderRec> out, boolean ins, LineItemRec li, OrderRec o){
                        out.collect(new JoinLineItemOrderRec(ins, o.getCustKey(), o.getOrderKey(), li.revenue()));
                    }
                });


        DataStream<JoinCustomerRevenueRec> custStream = liOr.keyBy(JoinLineItemOrderRec::getCustKey)
                .connect(cu.keyBy(CustomerRec::getCustKey))
                .process(new CoProcessFunction<JoinLineItemOrderRec, CustomerRec, JoinCustomerRevenueRec>(){
                    private transient MapState<Integer, Double> revenues;   // orderKey -> rev
                    private transient ValueState<CustomerRec> customer;
                    @Override public void open(Configuration c){
                        revenues = getRuntimeContext().getMapState(new MapStateDescriptor<>("rev", Integer.class, Double.class));
                        customer = getRuntimeContext().getState(new ValueStateDescriptor<>("cu", CustomerRec.class));
                    }
                    @Override public void processElement1(JoinLineItemOrderRec rec, Context ctx, Collector<JoinCustomerRevenueRec> out) throws Exception {
                        double sign = rec.isInsert()?1.0:-1.0;
                        revenues.put(rec.getOrderKey(), revenues.get(rec.getOrderKey())==null?sign*rec.getRevenue():revenues.get(rec.getOrderKey())+sign*rec.getRevenue());
                        CustomerRec c = customer.value();
                        if (c != null) out.collect(build(rec.isInsert(), rec, c));
                    }
                    @Override public void processElement2(CustomerRec c, Context ctx, Collector<JoinCustomerRevenueRec> out) throws Exception {
                        CustomerRec prev = customer.value();
                        customer.update(c.isInsert()?c:null);
                        if (c.isInsert()){
                            for (Integer ok : revenues.keys()){
                                JoinLineItemOrderRec stub = new JoinLineItemOrderRec(true, c.getCustKey(), ok, revenues.get(ok));
                                out.collect(build(true, stub, c));
                            }
                        }else if (prev!=null){ // delete customer -> retract all
                            for (Integer ok : revenues.keys()){
                                JoinLineItemOrderRec stub = new JoinLineItemOrderRec(false, prev.getCustKey(), ok, revenues.get(ok));
                                out.collect(build(false, stub, prev));
                            }
                        }
                    }
                    private JoinCustomerRevenueRec build(boolean ins, JoinLineItemOrderRec r, CustomerRec c){
                        return new JoinCustomerRevenueRec(ins, c.getNationKey(), c.getCustKey(), r.getOrderKey(),
                                c.getName(), c.getAcctBal(), c.getAddress(), c.getPhone(), c.getComment(), r.getRevenue());
                    }
                });


        DataStream<JoinCustNationRec> full = custStream.keyBy(JoinCustomerRevenueRec::getNationKey)
                .connect(na.keyBy(NationRec::getNationKey))
                .process(new CoProcessFunction<JoinCustomerRevenueRec, NationRec, JoinCustNationRec>(){
                    private transient MapState<Integer, JoinCustomerRevenueRec> buffered; // orderKey -> rec
                    private transient ValueState<NationRec> nation;
                    @Override public void open(Configuration c){
                        buffered = getRuntimeContext().getMapState(new MapStateDescriptor<>("buf", Integer.class, JoinCustomerRevenueRec.class));
                        nation   = getRuntimeContext().getState(new ValueStateDescriptor<>("na", NationRec.class));
                    }
                    @Override public void processElement1(JoinCustomerRevenueRec rec, Context ctx, Collector<JoinCustNationRec> out) throws Exception {
                        buffered.put(rec.getOrderKey(), rec);
                        NationRec n = nation.value();
                        if (n!=null) out.collect(toOut(rec, n));
                    }
                    @Override public void processElement2(NationRec n, Context ctx, Collector<JoinCustNationRec> out) throws Exception {
                        NationRec prev = nation.value();
                        nation.update(n.isInsert()?n:null);
                        if (n.isInsert()){
                            for (JoinCustomerRevenueRec r : buffered.values()) out.collect(toOut(r, n));
                        } else if (prev!=null){
                            for (JoinCustomerRevenueRec r : buffered.values()) out.collect(toOut(r, prev, false));
                        }
                    }
                    private JoinCustNationRec toOut(JoinCustomerRevenueRec r, NationRec n){ return toOut(r,n,r.isInsert()); }
                    private JoinCustNationRec toOut(JoinCustomerRevenueRec r, NationRec n, boolean ins){
                        return new JoinCustNationRec(ins, r.getCustKey(), r.getOrderKey(),
                                r.getCustName(), r.getAcctBal(), n.getName(),
                                r.getAddress(), r.getPhone(), r.getComment(), r.getRevenue());
                    }
                });


        DataStream<CustomerRevenueResult> result = full.keyBy(JoinCustNationRec::getCustKey)
                .process(new KeyedProcessFunction<Integer, JoinCustNationRec, CustomerRevenueResult>(){
                    private transient ValueState<Double> revenueSum;
                    private transient ValueState<JoinCustNationRec> snapshot;
                    @Override public void open(Configuration c){
                        revenueSum = getRuntimeContext().getState(new ValueStateDescriptor<>("sum", Double.class));
                        snapshot  = getRuntimeContext().getState(new ValueStateDescriptor<>("snap", JoinCustNationRec.class));
                    }
                    @Override public void processElement(JoinCustNationRec rec, Context ctx, Collector<CustomerRevenueResult> out) throws Exception {
                        double sum = revenueSum.value()==null?0.0:revenueSum.value();
                        sum += rec.isInsert()?rec.getRevenue():-rec.getRevenue();
                        revenueSum.update(sum);
                        snapshot.update(rec); // keep last to access static fields
                        JoinCustNationRec s = snapshot.value();
                        out.collect(new CustomerRevenueResult(rec.getCustKey(), s.getCustName(), sum, s.getAcctBal(),
                                s.getNation(), s.getAddress(), s.getPhone(), s.getComment()));
                    }
                });


        Path p = new Path(output);
        FileSystem fs = FileSystem.get(p.toUri());
        if (fs.exists(p)) fs.delete(p,false);
        result.writeAsText(output, FileSystem.WriteMode.OVERWRITE);

        env.execute("TPC‑H Q10 Incremental");
    }

    // ----------- map helpers ------------------------------------------
    private static MapFunction<String, LineItemRec> toLineItem(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new LineItemRec(ins,
                    Integer.parseInt(f[0]),        // orderKey
                    Integer.parseInt(f[3]),        // lineNumber
                    Double.parseDouble(f[5]),       // extendedPrice
                    Double.parseDouble(f[6]),       // discount
                    f[8].charAt(0));                // returnFlag
        };}

    private static MapFunction<String, OrderRec> toOrder(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new OrderRec(ins, Integer.parseInt(f[0]), Integer.parseInt(f[1]), LocalDate.parse(f[4]));
        };}

    private static MapFunction<String, CustomerRec> toCustomer(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new CustomerRec(ins,
                    Integer.parseInt(f[0]),
                    f[1],
                    Double.parseDouble(f[5]),
                    Integer.parseInt(f[3]),
                    f[2],
                    f[4],
                    f[7]);
        };
    }


    private static MapFunction<String, NationRec> toNation(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new NationRec(ins, Integer.parseInt(f[0]), f[1]);
        };}
}
