package org.example.q14;

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
import org.example.q14.model.LineItemRec;
import org.example.q14.model.PartRec;
import org.example.q14.model.JoinLineItemPartRec;
import org.example.q14.model.PromoRevenueResult;

import java.time.LocalDate;


public class StreamTpchQ14Job {


    private static final OutputTag<String> TAG_LI = new OutputTag<String>("LI"){};
    private static final OutputTag<String> TAG_PA = new OutputTag<String>("PA"){};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: <inputFile> <outputFile>");
            return;
        }
        final String input = args[0];
        final String output = args[1];

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(3);


        SingleOutputStreamOperator<String> head = env.readTextFile(input)
                .process(new ProcessFunction<String, String>() {
                    @Override public void processElement(String v, Context ctx, Collector<String> out){
                        if (v.length() < 3) return;
                        String tag = v.substring(1,3);
                        String body = v.charAt(0) + v.substring(3);
                        switch(tag){
                            case "LI": ctx.output(TAG_LI, body); break;
                            case "PA": ctx.output(TAG_PA, body); break;
                        }
                    }
                });


        DataStream<LineItemRec> li = head.getSideOutput(TAG_LI).map(toLineItem());
        DataStream<PartRec>     pa = head.getSideOutput(TAG_PA).map(toPart());


        LocalDate lower = LocalDate.parse("1995-09-01");
        LocalDate upper = LocalDate.parse("1995-10-01");
        li = li.filter(r -> !r.getShipDate().isBefore(lower) && r.getShipDate().isBefore(upper));


        DataStream<JoinLineItemPartRec> join = li.keyBy(LineItemRec::getPartKey)
                .connect(pa.keyBy(PartRec::getPartKey))
                .process(new CoProcessFunction<LineItemRec, PartRec, JoinLineItemPartRec>(){
                    private transient MapState<Integer, LineItemRec> items;
                    private transient ValueState<PartRec> part;
                    @Override public void open(Configuration c){
                        items = getRuntimeContext().getMapState(new MapStateDescriptor<>("li", Integer.class, LineItemRec.class));
                        part  = getRuntimeContext().getState(new ValueStateDescriptor<>("pa", PartRec.class));
                    }
                    @Override public void processElement1(LineItemRec li, Context ctx, Collector<JoinLineItemPartRec> out) throws Exception {
                        if (li.isInsert()) {
                            items.put(li.getLineNo(), li);
                            if (part.value()!=null) emit(out, true, li, part.value());
                        } else {
                            items.remove(li.getLineNo());
                            if (part.value()!=null) emit(out, false, li, part.value());
                        }
                    }
                    @Override public void processElement2(PartRec p, Context ctx, Collector<JoinLineItemPartRec> out) throws Exception {
                        if (p.isInsert()) {
                            part.update(p);
                            for (LineItemRec li : items.values()) emit(out, true, li, p);
                        } else {
                            part.clear();
                            for (LineItemRec li : items.values()) emit(out, false, li, p);
                        }
                    }
                    private void emit(Collector<JoinLineItemPartRec> out, boolean ins, LineItemRec li, PartRec p){
                        double total = li.getExtendedPrice() * (1 - li.getDiscount());
                        double promo = p.getType().startsWith("PROMO") ? total : 0.0;
                        out.collect(new JoinLineItemPartRec(ins, total, promo));
                    }
                });


        DataStream<PromoRevenueResult> result = join.keyBy(rec -> 0) // global key
                .process(new KeyedProcessFunction<Integer, JoinLineItemPartRec, PromoRevenueResult>(){
                    private transient ValueState<Double> totalSum;
                    private transient ValueState<Double> promoSum;
                    @Override public void open(Configuration c){
                        totalSum = getRuntimeContext().getState(new ValueStateDescriptor<>("tot", Double.class));
                        promoSum = getRuntimeContext().getState(new ValueStateDescriptor<>("pro", Double.class));
                    }
                    @Override public void processElement(JoinLineItemPartRec rec, Context ctx, Collector<PromoRevenueResult> out) throws Exception {
                        double tot = totalSum.value()==null?0:totalSum.value();
                        double pro = promoSum.value()==null?0:promoSum.value();
                        if (rec.isInsert()) {
                            tot += rec.getTotal();
                            pro += rec.getPromo();
                        } else {
                            tot -= rec.getTotal();
                            pro -= rec.getPromo();
                        }
                        totalSum.update(tot);
                        promoSum.update(pro);
                        double ratio = tot==0?0: (100.0 * pro / tot);
                        out.collect(new PromoRevenueResult(ratio));
                    }
                });


        Path p = new Path(output);
        FileSystem fs = FileSystem.get(p.toUri());
        if (fs.exists(p)) fs.delete(p,false);
        result.writeAsText(output, FileSystem.WriteMode.OVERWRITE);

        env.execute("TPC‑H Q14 Incremental");
    }

    // ---------- map helpers ---------------------------------------------
    private static MapFunction<String, LineItemRec> toLineItem(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new LineItemRec(ins,
                    Integer.parseInt(f[1]), // partKey index1
                    Double.parseDouble(f[5]),
                    Double.parseDouble(f[6]),
                    LocalDate.parse(f[10]),
                    Integer.parseInt(f[3]));
        };}

    private static MapFunction<String, PartRec> toPart(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new PartRec(ins, Integer.parseInt(f[0]), f[4]);
        };}
}
