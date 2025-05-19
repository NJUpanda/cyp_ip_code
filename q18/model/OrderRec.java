package org.example.q18.model;

import java.io.Serializable;
import java.time.LocalDate;


public class OrderRec implements Serializable {
    private boolean  insert;
    private int      orderKey;
    private int      custKey;
    private LocalDate orderDate;
    private double   totalPrice;

    public OrderRec(){}

    public OrderRec(boolean insert, int orderKey, int custKey, LocalDate orderDate, double totalPrice){
        this.insert     = insert;
        this.orderKey   = orderKey;
        this.custKey    = custKey;
        this.orderDate  = orderDate;
        this.totalPrice = totalPrice;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public LocalDate getOrderDate(){ return orderDate; }
    public void      setOrderDate(LocalDate d){ orderDate = d; }

    public double getTotalPrice(){ return totalPrice; }
    public void   setTotalPrice(double v){ totalPrice = v; }

    @Override public String toString(){
        return "OrderRec{" +
                "ins=" + insert +
                ", okey=" + orderKey +
                ", ckey=" + custKey +
                ", date=" + orderDate +
                ", price=" + totalPrice +
                '}';
    }
}
