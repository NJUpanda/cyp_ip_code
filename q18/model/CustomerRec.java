package org.example.q18.model;

import java.io.Serializable;


public class CustomerRec implements Serializable {
    private boolean insert;
    private int     custKey;
    private String  name;

    public CustomerRec(){}

    public CustomerRec(boolean insert, int custKey, String name){
        this.insert  = insert;
        this.custKey = custKey;
        this.name    = name;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public String getName(){ return name; }
    public void   setName(String v){ name = v; }

    @Override public String toString(){
        return "CustomerRec{" +
                "ins=" + insert +
                ", ckey=" + custKey +
                ", name='" + name + '\'' +
                '}';
    }
}
