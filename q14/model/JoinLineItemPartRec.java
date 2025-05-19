package org.example.q14.model;

import java.io.Serializable;


public class JoinLineItemPartRec implements Serializable {
    private boolean insert;
    private double  total;
    private double  promo;

    public JoinLineItemPartRec() {}

    public JoinLineItemPartRec(boolean insert, double total, double promo) {
        this.insert = insert;
        this.total  = total;
        this.promo  = promo;
    }

    public boolean isInsert() { return insert; }
    public void    setInsert(boolean b){ this.insert=b; }

    public double  getTotal() { return total; }
    public void    setTotal(double t){ this.total=t; }

    public double  getPromo() { return promo; }
    public void    setPromo(double p){ this.promo=p; }

    @Override
    public String toString(){
        return "JoinLineItemPartRec{" +
                "ins=" + insert +
                ", total=" + total +
                ", promo=" + promo +
                '}';
    }
}
