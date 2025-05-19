package org.example.q10.model;

import java.io.Serializable;


public class NationRec implements Serializable {
    private boolean insert;
    private int     nationKey;
    private String  name;

    public NationRec(){}

    public NationRec(boolean insert, int nationKey, String name){
        this.insert    = insert;
        this.nationKey = nationKey;
        this.name      = name;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getNationKey(){ return nationKey; }
    public void setNationKey(int v){ nationKey = v; }

    public String getName(){ return name; }
    public void   setName(String v){ name = v; }

    @Override public String toString(){
        return "NationRec{" +
                "ins=" + insert +
                ", nkey=" + nationKey +
                ", name='" + name + '\'' +
                '}';
    }
}
