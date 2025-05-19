package org.example.q12.model;

import java.io.Serializable;


public class ShipModePriorityResult implements Serializable {
    private String shipMode;
    private long   highLineCount;
    private long   lowLineCount;

    public ShipModePriorityResult() {}

    public ShipModePriorityResult(String shipMode, long highLineCount, long lowLineCount) {
        this.shipMode      = shipMode;
        this.highLineCount = highLineCount;
        this.lowLineCount  = lowLineCount;
    }

    public String getShipMode() { return shipMode; }
    public void   setShipMode(String shipMode) { this.shipMode = shipMode; }

    public long getHighLineCount() { return highLineCount; }
    public void setHighLineCount(long highLineCount) { this.highLineCount = highLineCount; }

    public long getLowLineCount() { return lowLineCount; }
    public void setLowLineCount(long lowLineCount) { this.lowLineCount = lowLineCount; }

    @Override public String toString() {
        return "ShipModePriorityResult{" +
                "shipMode='" + shipMode + '\'' +
                ", highLineCount=" + highLineCount +
                ", lowLineCount=" + lowLineCount +
                '}';
    }
}
