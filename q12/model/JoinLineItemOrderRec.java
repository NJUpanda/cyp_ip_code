package org.example.q12.model;

import java.io.Serializable;


public class JoinLineItemOrderRec implements Serializable {
    private boolean insert;
    private String  shipMode;
    private long    highCnt;
    private long    lowCnt;

    public JoinLineItemOrderRec() {}

    public JoinLineItemOrderRec(boolean insert, String shipMode, long highCnt, long lowCnt) {
        this.insert  = insert;
        this.shipMode = shipMode;
        this.highCnt = highCnt;
        this.lowCnt  = lowCnt;
    }

    public boolean isInsert() { return insert; }
    public void    setInsert(boolean insert) { this.insert = insert; }

    public String getShipMode() { return shipMode; }
    public void   setShipMode(String shipMode) { this.shipMode = shipMode; }

    public long getHighCnt() { return highCnt; }
    public void setHighCnt(long highCnt) { this.highCnt = highCnt; }

    public long getLowCnt() { return lowCnt; }
    public void setLowCnt(long lowCnt) { this.lowCnt = lowCnt; }

    @Override public String toString() {
        return "JoinLineItemOrderRec{" +
                "ins=" + insert +
                ", shipMode='" + shipMode + '\'' +
                ", highCnt=" + highCnt +
                ", lowCnt=" + lowCnt +
                '}';
    }
}
