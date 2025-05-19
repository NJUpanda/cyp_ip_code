package org.example.q18.model;

import java.io.Serializable;


public class LineItemRec implements Serializable {
    private boolean insert;
    private int     orderKey;
    private int     lineNumber;
    private double  quantity;

    public LineItemRec(){}

    public LineItemRec(boolean insert, int orderKey, int lineNumber, double quantity){
        this.insert     = insert;
        this.orderKey   = orderKey;
        this.lineNumber = lineNumber;
        this.quantity   = quantity;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public int getLineNumber(){ return lineNumber; }
    public void setLineNumber(int v){ lineNumber = v; }

    public double getQuantity(){ return quantity; }
    public void   setQuantity(double v){ quantity = v; }

    @Override public String toString(){
        return "LineItemRec{" +
                "ins=" + insert +
                ", okey=" + orderKey +
                ", lno=" + lineNumber +
                ", qty=" + quantity +
                '}';
    }
}
