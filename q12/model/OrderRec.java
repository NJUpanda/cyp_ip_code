package org.example.q12.model;

import java.io.Serializable;


public class OrderRec implements Serializable {
    private boolean insert;      // true = Insert, false = Delete
    private int     orderKey;    // O_ORDERKEY
    private String  orderPriority; // O_ORDERPRIORITY

    public OrderRec() {}

    public OrderRec(boolean insert, int orderKey, String orderPriority) {
        this.insert         = insert;
        this.orderKey       = orderKey;
        this.orderPriority  = orderPriority;
    }

    // ---------------- getters / setters -----------------------------
    public boolean isInsert() { return insert; }
    public void    setInsert(boolean insert) { this.insert = insert; }

    public int getOrderKey() { return orderKey; }
    public void setOrderKey(int orderKey) { this.orderKey = orderKey; }

    public String getOrderPriority() { return orderPriority; }
    public void   setOrderPriority(String orderPriority) { this.orderPriority = orderPriority; }

    // ---------------- helper ----------------------------------------
    public boolean isHighPriority() {
        return "1-URGENT".equals(orderPriority) || "2-HIGH".equals(orderPriority);
    }

    @Override public String toString() {
        return "OrderRec{" +
                "ins=" + insert +
                ", orderKey=" + orderKey +
                ", orderPriority='" + orderPriority + '\'' +
                '}';
    }
}
