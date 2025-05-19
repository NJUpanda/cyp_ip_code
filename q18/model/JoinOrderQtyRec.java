package org.example.q18.model;

import java.io.Serializable;
import java.time.LocalDate;


public class JoinOrderQtyRec implements Serializable {
    private boolean insert;
    private int     custKey;
    private int     orderKey;
    private LocalDate orderDate;
    private double  totalPrice;
    private double  sumQty;

    public JoinOrderQtyRec(){}

    public JoinOrderQtyRec(boolean insert, int custKey, int orderKey,
                           LocalDate orderDate, double totalPrice, double sumQty){
        this.insert     = insert;
        this.custKey    = custKey;
        this.orderKey   = orderKey;
        this.orderDate  = orderDate;
        this.totalPrice = totalPrice;
        this.sumQty     = sumQty;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public LocalDate getOrderDate(){ return orderDate; }
    public void      setOrderDate(LocalDate d){ orderDate = d; }

    public double getTotalPrice(){ return totalPrice; }
    public void   setTotalPrice(double v){ totalPrice = v; }

    public double getSumQty(){ return sumQty; }
    public void   setSumQty(double v){ sumQty = v; }

    @Override public String toString(){
        return "JoinOrderQtyRec{" +
                "ins=" + insert +
                ", ckey=" + custKey +
                ", okey=" + orderKey +
                ", qty=" + sumQty +
                '}';
    }
}
