package org.example.q18.model;

import java.io.Serializable;


public class OrderQtyRec implements Serializable {
    private boolean insert;
    private int     orderKey;
    private double  sumQty;

    public OrderQtyRec(){}

    public OrderQtyRec(boolean insert, int orderKey, double sumQty){
        this.insert   = insert;
        this.orderKey = orderKey;
        this.sumQty   = sumQty;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public double getSumQty(){ return sumQty; }
    public void   setSumQty(double v){ sumQty = v; }

    @Override public String toString(){
        return "OrderQtyRec{" +
                "ins=" + insert +
                ", okey=" + orderKey +
                ", qty=" + sumQty +
                '}';
    }
}
