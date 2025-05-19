package org.example.q14.model;

import java.io.Serializable;
import java.time.LocalDate;


public class LineItemRec implements Serializable {
    private boolean   insert;
    private int       partKey;
    private double    extendedPrice;
    private double    discount;
    private LocalDate shipDate;
    private int       lineNo;

    // -------------- constructors --------------------------
    public LineItemRec() {}

    public LineItemRec(boolean insert,
                       int partKey,
                       double extendedPrice,
                       double discount,
                       LocalDate shipDate,
                       int lineNo) {
        this.insert        = insert;
        this.partKey       = partKey;
        this.extendedPrice = extendedPrice;
        this.discount      = discount;
        this.shipDate      = shipDate;
        this.lineNo        = lineNo;
    }

    // -------------- getters / setters ---------------------
    public boolean isInsert()               { return insert; }
    public void    setInsert(boolean b)     { this.insert = b; }

    public int     getPartKey()             { return partKey; }
    public void    setPartKey(int k)        { this.partKey = k; }

    public double  getExtendedPrice()       { return extendedPrice; }
    public void    setExtendedPrice(double p){ this.extendedPrice = p; }

    public double  getDiscount()            { return discount; }
    public void    setDiscount(double d)    { this.discount = d; }

    public LocalDate getShipDate()          { return shipDate; }
    public void      setShipDate(LocalDate d){ this.shipDate = d; }

    public int     getLineNo()              { return lineNo; }
    public void    setLineNo(int n)         { this.lineNo = n; }

    // -------------- helper -------------------------------

    public double salesAmount() { return extendedPrice * (1 - discount); }

    @Override public String toString() {
        return "LineItemRec{" +
                "ins=" + insert +
                ", partKey=" + partKey +
                ", extendedPrice=" + extendedPrice +
                ", discount=" + discount +
                ", shipDate=" + shipDate +
                ", lineNo=" + lineNo +
                '}';
    }
}
