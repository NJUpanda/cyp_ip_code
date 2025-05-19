package org.example.q14.model;

import java.io.Serializable;


public class PartRec implements Serializable {
    private boolean insert;
    private int     partKey;
    private String  type;

    public PartRec() {}

    public PartRec(boolean insert, int partKey, String type) {
        this.insert  = insert;
        this.partKey = partKey;
        this.type    = type;
    }

    public boolean isInsert()            { return insert; }
    public void    setInsert(boolean b)  { this.insert = b; }

    public int     getPartKey()          { return partKey; }
    public void    setPartKey(int k)     { this.partKey = k; }

    public String  getType()             { return type; }
    public void    setType(String t)     { this.type = t; }


    public boolean isPromo() { return type != null && type.startsWith("PROMO"); }

    @Override public String toString() {
        return "PartRec{" +
                "ins=" + insert +
                ", partKey=" + partKey +
                ", type='" + type + '\'' +
                '}';
    }
}
