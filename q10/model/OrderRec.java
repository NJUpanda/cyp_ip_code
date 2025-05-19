package org.example.q10.model;

import java.io.Serializable;
import java.time.LocalDate;


public class OrderRec implements Serializable {
    private boolean  insert;
    private int      orderKey;
    private int      custKey;
    private LocalDate orderDate;

    public OrderRec() {}

    public OrderRec(boolean insert, int orderKey, int custKey, LocalDate orderDate){
        this.insert    = insert;
        this.orderKey  = orderKey;
        this.custKey   = custKey;
        this.orderDate = orderDate;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public LocalDate getOrderDate(){ return orderDate; }
    public void      setOrderDate(LocalDate d){ orderDate = d; }

    @Override public String toString(){
        return "OrderRec{" +
                "ins=" + insert +
                ", okey=" + orderKey +
                ", ckey=" + custKey +
                ", odate=" + orderDate +
                '}';
    }
}
