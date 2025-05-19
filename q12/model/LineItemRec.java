package org.example.q12.model;

import java.io.Serializable;
import java.time.LocalDate;


public class LineItemRec implements Serializable {
    private boolean   insert;
    private int       orderKey;
    private int       lineNo;
    private String    shipMode;
    private LocalDate shipDate;
    private LocalDate commitDate;
    private LocalDate receiptDate;

    public LineItemRec() {}

    public LineItemRec(boolean insert,
                       int orderKey,
                       int lineNo,
                       String shipMode,
                       LocalDate shipDate,
                       LocalDate commitDate,
                       LocalDate receiptDate) {
        this.insert      = insert;
        this.orderKey    = orderKey;
        this.lineNo      = lineNo;
        this.shipMode    = shipMode;
        this.shipDate    = shipDate;
        this.commitDate  = commitDate;
        this.receiptDate = receiptDate;
    }

    // ------------ getters / setters ---------------------------------
    public boolean isInsert() { return insert; }
    public void    setInsert(boolean insert) { this.insert = insert; }

    public int getOrderKey() { return orderKey; }
    public void setOrderKey(int orderKey) { this.orderKey = orderKey; }

    public int getLineNo() { return lineNo; }
    public void setLineNo(int lineNo) { this.lineNo = lineNo; }

    public String getShipMode() { return shipMode; }
    public void   setShipMode(String shipMode) { this.shipMode = shipMode; }

    public LocalDate getShipDate() { return shipDate; }
    public void      setShipDate(LocalDate shipDate) { this.shipDate = shipDate; }

    public LocalDate getCommitDate() { return commitDate; }
    public void      setCommitDate(LocalDate commitDate) { this.commitDate = commitDate; }

    public LocalDate getReceiptDate() { return receiptDate; }
    public void      setReceiptDate(LocalDate receiptDate) { this.receiptDate = receiptDate; }

    // ------------ helper --------------------------------------------

    public boolean validShipMode() {
        return "MAIL".equals(shipMode) || "SHIP".equals(shipMode);
    }

    @Override public String toString() {
        return "LineItemRec{" +
                "ins=" + insert +
                ", orderKey=" + orderKey +
                ", lineNo=" + lineNo +
                ", shipMode='" + shipMode + '\'' +
                ", shipDate=" + shipDate +
                ", commitDate=" + commitDate +
                ", receiptDate=" + receiptDate +
                '}';
    }
}
