package org.example.q10.model;

import java.io.Serializable;


public class LineItemRec implements Serializable {
    private boolean insert;
    private int     orderKey;
    private int     lineNumber;
    private double  extendedPrice;
    private double  discount;
    private char    returnFlag;

    public LineItemRec() {}

    public LineItemRec(boolean insert,
                       int orderKey,
                       int lineNumber,
                       double extendedPrice,
                       double discount,
                       char returnFlag) {
        this.insert        = insert;
        this.orderKey      = orderKey;
        this.lineNumber    = lineNumber;
        this.extendedPrice = extendedPrice;
        this.discount      = discount;
        this.returnFlag    = returnFlag;
    }

    public boolean isInsert()     { return insert; }
    public void    setInsert(boolean insert) { this.insert = insert; }

    public int getOrderKey()      { return orderKey; }
    public void setOrderKey(int v){ this.orderKey = v; }

    public int getLineNumber()    { return lineNumber; }
    public void setLineNumber(int v){ this.lineNumber = v; }

    public double getExtendedPrice() { return extendedPrice; }
    public void   setExtendedPrice(double v){ this.extendedPrice = v; }

    public double getDiscount()   { return discount; }
    public void   setDiscount(double v){ this.discount = v; }

    public char getReturnFlag()   { return returnFlag; }
    public void setReturnFlag(char v){ this.returnFlag = v; }

    // ---------- helpers ----------------------------------------------
    public boolean isReturned(){ return returnFlag == 'R'; }

    public double revenue(){ return extendedPrice * (1.0 - discount); }

    @Override public String toString(){
        return "LineItemRec{" +
                "ins=" + insert +
                ", okey=" + orderKey +
                ", lno=" + lineNumber +
                ", price=" + extendedPrice +
                ", disc=" + discount +
                ", flag=" + returnFlag +
                '}';
    }
}
