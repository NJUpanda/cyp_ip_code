package org.example.q10.model;

import java.io.Serializable;


public class JoinLineItemOrderRec implements Serializable {
    private boolean insert;
    private int     custKey;
    private int     orderKey;
    private double  revenue;

    public JoinLineItemOrderRec(){}

    public JoinLineItemOrderRec(boolean insert, int custKey, int orderKey, double revenue){
        this.insert   = insert;
        this.custKey  = custKey;
        this.orderKey = orderKey;
        this.revenue  = revenue;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public double getRevenue(){ return revenue; }
    public void   setRevenue(double v){ revenue = v; }

    @Override public String toString(){
        return "JoinLIOR{" +
                "ins=" + insert +
                ", ckey=" + custKey +
                ", okey=" + orderKey +
                ", rev=" + revenue +
                '}';
    }
}
