package org.example.q12;

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
import org.example.q12.model.LineItemRec;
import org.example.q12.model.OrderRec;
import org.example.q12.model.JoinLineItemOrderRec;
import org.example.q12.model.ShipModePriorityResult;

import java.time.LocalDate;


public class StreamTpchQ12Job {

    // side‑output tags ----------------------------------------------------
    private static final OutputTag<String> TAG_LI = new OutputTag<String>("LI"){};
    private static final OutputTag<String> TAG_OR = new OutputTag<String>("OR"){};

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: <inputFile> <outputFile>");
            return;
        }
        final String input  = args[0];
        final String output = args[1];

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();



        SingleOutputStreamOperator<String> head = env.readTextFile(input)
                .process(new ProcessFunction<String, String>() {
                    @Override public void processElement(String v, Context ctx, Collector<String> out){
                        if (v.length() < 3) return;
                        String tag  = v.substring(1,3); // e.g. "LI" / "OR"
                        String body = v.charAt(0) + v.substring(3); // 保留首字符的 +/-
                        switch(tag){
                            case "LI": ctx.output(TAG_LI, body); break;
                            case "OR": ctx.output(TAG_OR, body); break;
                        }
                    }
                });


        DataStream<LineItemRec> li = head.getSideOutput(TAG_LI).map(toLineItem());
        DataStream<OrderRec>    or = head.getSideOutput(TAG_OR).map(toOrder());


        LocalDate lower = LocalDate.parse("1994-01-01");
        LocalDate upper = LocalDate.parse("1995-01-01");
        li = li.filter(r -> r.validShipMode()
                && r.getCommitDate().isBefore(r.getReceiptDate())
                && r.getShipDate().isBefore(r.getCommitDate())
                && (!r.getReceiptDate().isBefore(lower))
                && r.getReceiptDate().isBefore(upper));


        DataStream<JoinLineItemOrderRec> join = li.keyBy(LineItemRec::getOrderKey)
                .connect(or.keyBy(OrderRec::getOrderKey))
                .process(new CoProcessFunction<LineItemRec, OrderRec, JoinLineItemOrderRec>(){
                    private transient MapState<Integer, LineItemRec> items; // key = lineNo
                    private transient ValueState<OrderRec> order;
                    @Override public void open(Configuration c){
                        items = getRuntimeContext().getMapState(new MapStateDescriptor<>("li", Integer.class, LineItemRec.class));
                        order = getRuntimeContext().getState(new ValueStateDescriptor<>("or", OrderRec.class));
                    }
                    @Override public void processElement1(LineItemRec li, Context ctx, Collector<JoinLineItemOrderRec> out) throws Exception {
                        if (li.isInsert()) {
                            items.put(li.getLineNo(), li);
                            OrderRec o = order.value();
                            if (o != null) emit(out, true, li, o);
                        } else {
                            items.remove(li.getLineNo());
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
                        boolean high = o.isHighPriority();
                        out.collect(new JoinLineItemOrderRec(ins,
                                li.getShipMode(),
                                high?1:0,
                                high?0:1));
                    }
                });


        DataStream<ShipModePriorityResult> result = join.keyBy(JoinLineItemOrderRec::getShipMode)
                .process(new KeyedProcessFunction<String, JoinLineItemOrderRec, ShipModePriorityResult>(){
                    private transient ValueState<Long> highSum;
                    private transient ValueState<Long> lowSum;
                    @Override public void open(Configuration c){
                        highSum = getRuntimeContext().getState(new ValueStateDescriptor<>("hi", Long.class));
                        lowSum  = getRuntimeContext().getState(new ValueStateDescriptor<>("lo", Long.class));
                    }
                    @Override public void processElement(JoinLineItemOrderRec rec, Context ctx, Collector<ShipModePriorityResult> out) throws Exception {
                        long hi = highSum.value()==null?0:highSum.value();
                        long lo = lowSum.value()==null?0:lowSum.value();
                        if (rec.isInsert()) {
                            hi += rec.getHighCnt();
                            lo += rec.getLowCnt();
                        } else {
                            hi -= rec.getHighCnt();
                            lo -= rec.getLowCnt();
                        }
                        highSum.update(hi);
                        lowSum.update(lo);
                        out.collect(new ShipModePriorityResult(ctx.getCurrentKey(), hi, lo));
                    }
                });


        Path p = new Path(output);
        FileSystem fs = FileSystem.get(p.toUri());
        if (fs.exists(p)) fs.delete(p,false);
        result.writeAsText(output, FileSystem.WriteMode.OVERWRITE);

        env.execute("TPC‑H Q12 Incremental");
    }

    // ---------- map helpers ---------------------------------------------
    private static MapFunction<String, LineItemRec> toLineItem(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new LineItemRec(ins,
                    Integer.parseInt(f[0]),   // orderKey index0
                    Integer.parseInt(f[3]),   // lineNo index3
                    f[14],                    // shipMode index14
                    LocalDate.parse(f[10]),   // shipDate index10
                    LocalDate.parse(f[11]),   // commitDate index11
                    LocalDate.parse(f[12]));  // receiptDate index12
        };}

    private static MapFunction<String, OrderRec> toOrder(){
        return raw -> {
            String[] f = raw.split("\\|");
            boolean ins = f[0].charAt(0)== '+';
            f[0]=f[0].substring(1);
            return new OrderRec(ins, Integer.parseInt(f[0]), f[5]);
        };}
}
