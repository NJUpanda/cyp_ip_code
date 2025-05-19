package org.example.q18.model;

import java.io.Serializable;
import java.time.LocalDate;


public class CustomerOrderQtyResult implements Serializable {
    private int     custKey;
    private String  custName;
    private int     orderKey;
    private LocalDate orderDate;
    private double  totalPrice;
    private double  sumQty;

    public CustomerOrderQtyResult(){}

    public CustomerOrderQtyResult(int custKey, String custName, int orderKey,
                                  LocalDate orderDate, double totalPrice, double sumQty){
        this.custKey    = custKey;
        this.custName   = custName;
        this.orderKey   = orderKey;
        this.orderDate  = orderDate;
        this.totalPrice = totalPrice;
        this.sumQty     = sumQty;
    }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public String getCustName(){ return custName; }
    public void   setCustName(String v){ custName = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public LocalDate getOrderDate(){ return orderDate; }
    public void      setOrderDate(LocalDate d){ orderDate = d; }

    public double getTotalPrice(){ return totalPrice; }
    public void   setTotalPrice(double v){ totalPrice = v; }

    public double getSumQty(){ return sumQty; }
    public void   setSumQty(double v){ sumQty = v; }

    @Override public String toString(){
        return "CustomerOrderQtyResult{" +
                "ckey=" + custKey +
                ", name='" + custName + '\'' +
                ", okey=" + orderKey +
                ", date=" + orderDate +
                ", price=" + totalPrice +
                ", qty=" + sumQty +
                '}';
    }
}
