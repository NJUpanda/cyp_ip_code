package org.example.q18;

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

// model imports
import org.example.q18.model.*;

import java.time.LocalDate;


public class StreamTpchQ18Job {

    private static final OutputTag<String> TAG_LI = new OutputTag<String>("LI"){};
    private static final OutputTag<String> TAG_OR = new OutputTag<String>("OR"){};
    private static final OutputTag<String> TAG_CU = new OutputTag<String>("CU"){};

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: <inputFile> <outputFile> <quantityThreshold>");
            return;
        }
        final String input   = args[0];
        final String output  = args[1];
        final double THR     = Double.parseDouble(args[2]);

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
                        }
                    }
                });


        DataStream<LineItemRec> li = head.getSideOutput(TAG_LI).map(toLineItem());
        DataStream<OrderRec>    or = head.getSideOutput(TAG_OR).map(toOrder());
        DataStream<CustomerRec> cu = head.getSideOutput(TAG_CU).map(toCustomer());


        DataStream<OrderQtyRec> orderQty = li.keyBy(LineItemRec::getOrderKey)
                .process(new KeyedProcessFunction<Integer, LineItemRec, OrderQtyRec>(){
                    private transient ValueState<Double> sumQty;
                    private transient ValueState<Double> emittedQty; // null if 不在结果集中
                    @Override public void open(Configuration c){
                        sumQty     = getRuntimeContext().getState(new ValueStateDescriptor<>("sum", Double.class));
                        emittedQty = getRuntimeContext().getState(new ValueStateDescriptor<>("emit", Double.class));
                    }
                    @Override public void processElement(LineItemRec rec, Context ctx, Collector<OrderQtyRec> out) throws Exception {
                        double s = sumQty.value()==null?0.0:sumQty.value();
                        s += rec.isInsert()?rec.getQuantity():-rec.getQuantity();
                        sumQty.update(s);
                        Double prevEmit = emittedQty.value();
                        boolean qualifies = s > THR;
                        if (qualifies){
                            if (prevEmit==null){
                                // 新进入结果集
                                emittedQty.update(s);
                                out.collect(new OrderQtyRec(true, ctx.getCurrentKey(), s));
                            } else if (Math.abs(prevEmit - s) > 1e-6){
                                // 更新值：先撤销旧值，再发送新值
                                out.collect(new OrderQtyRec(false, ctx.getCurrentKey(), prevEmit));
                                out.collect(new OrderQtyRec(true, ctx.getCurrentKey(), s));
                                emittedQty.update(s);
                            }
                        } else {
                            if (prevEmit!=null){
                                // 退出结果集
                                out.collect(new OrderQtyRec(false, ctx.getCurrentKey(), prevEmit));
                                emittedQty.clear();
                            }
                        }
                    }
                });


        DataStream<JoinOrderQtyRec> oq = orderQty.keyBy(OrderQtyRec::getOrderKey)
                .connect(or.keyBy(OrderRec::getOrderKey))
                .process(new CoProcessFunction<OrderQtyRec, OrderRec, JoinOrderQtyRec>(){
                    private transient MapState<Integer, OrderQtyRec> qtyBuf; // orderKey unique -> record (kept for updates)
                    private transient ValueState<OrderRec> order;
                    @Override public void open(Configuration c){
                        qtyBuf = getRuntimeContext().getMapState(new MapStateDescriptor<>("qty", Integer.class, OrderQtyRec.class));
                        order  = getRuntimeContext().getState(new ValueStateDescriptor<>("or", OrderRec.class));
                    }
                    @Override public void processElement1(OrderQtyRec rec, Context ctx, Collector<JoinOrderQtyRec> out) throws Exception {
                        qtyBuf.put(rec.getOrderKey(), rec); // only one per key, replace
                        OrderRec o = order.value();
                        if (o != null){ emit(out, rec.isInsert(), rec, o); }
                    }
                    @Override public void processElement2(OrderRec o, Context ctx, Collector<JoinOrderQtyRec> out) throws Exception {
                        OrderRec prev = order.value();
                        order.update(o.isInsert()?o:null);
                        if (o.isInsert()){
                            OrderQtyRec q = qtyBuf.get(o.getOrderKey());
                            if (q!=null) emit(out, true, q, o);
                        } else if (prev!=null){
                            OrderQtyRec q = qtyBuf.get(o.getOrderKey());
                            if (q!=null) emit(out, false, q, prev);
                        }
                    }
                    private void emit(Collector<JoinOrderQtyRec> out, boolean ins, OrderQtyRec q, OrderRec o){
                        out.collect(new JoinOrderQtyRec(ins, o.getCustKey(), o.getOrderKey(), o.getOrderDate(), o.getTotalPrice(), q.getSumQty()));
                    }
                });


        DataStream<CustomerOrderQtyResult> result = oq.keyBy(JoinOrderQtyRec::getCustKey)
                .connect(cu.keyBy(CustomerRec::getCustKey))
                .process(new CoProcessFunction<JoinOrderQtyRec, CustomerRec, CustomerOrderQtyResult>(){
                    private transient MapState<Integer, JoinOrderQtyRec> buf; // orderKey -> rec
                    private transient ValueState<CustomerRec> cust;
                    @Override public void open(Configuration c){
                        buf  = getRuntimeContext().getMapState(new MapStateDescriptor<>("buf", Integer.class, JoinOrderQtyRec.class));
                        cust = getRuntimeContext().getState(new ValueStateDescriptor<>("cu", CustomerRec.class));
                    }
                    @Override public void processElement1(JoinOrderQtyRec rec, Context ctx, Collector<CustomerOrderQtyResult> out) throws Exception {
                        buf.put(rec.getOrderKey(), rec);
                        CustomerRec c = cust.value();
                        if (c!=null) emit(out, rec.isInsert(), rec, c);
                    }
                    @Override public void processElement2(CustomerRec c, Context ctx, Collector<CustomerOrderQtyResult> out) throws Exception {
                        CustomerRec prev = cust.value();
                        cust.update(c.isInsert()?c:null);
                        if (c.isInsert()){
                            for (JoinOrderQtyRec r : buf.values()) emit(out, true, r, c);
                        } else if (prev!=null){
                            for (JoinOrderQtyRec r : buf.values()) emit(out, false, r, prev);
                        }
                    }
                    private void emit(Collector<CustomerOrderQtyResult> out, boolean ins, JoinOrderQtyRec r, CustomerRec c){
                        if (ins){
                            out.collect(new CustomerOrderQtyResult(c.getCustKey(), c.getName(), r.getOrderKey(), r.getOrderDate(), r.getTotalPrice(), r.getSumQty()));
                        } else {

                        }
                    }
                });


        Path p = new Path(output);
        FileSystem fs = FileSystem.get(p.toUri());
        if (fs.exists(p)) fs.delete(p,false);
        result.writeAsText(output, FileSystem.WriteMode.OVERWRITE);

        env.execute("TPC-H Q18 Incremental");
    }

    // ------------------ Map helpers -----------------------------------
    private static MapFunction<String, LineItemRec> toLineItem(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0) == '+';
            f[0] = f[0].substring(1);
            return new LineItemRec(ins,
                    Integer.parseInt(f[0]),    // orderKey
                    Integer.parseInt(f[3]),    // lineNumber
                    Double.parseDouble(f[4])); // quantity
        };}

    private static MapFunction<String, OrderRec> toOrder(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0) == '+';
            f[0] = f[0].substring(1);
            return new OrderRec(ins,
                    Integer.parseInt(f[0]),    // orderKey
                    Integer.parseInt(f[1]),    // custKey
                    LocalDate.parse(f[4]),      // orderDate
                    Double.parseDouble(f[3]));  // totalPrice
        };}

    private static MapFunction<String, CustomerRec> toCustomer(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0) == '+';
            f[0] = f[0].substring(1);
            return new CustomerRec(ins,
                    Integer.parseInt(f[0]),  // custKey
                    f[1]);                   // name
        };}
}
