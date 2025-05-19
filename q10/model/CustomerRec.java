package org.example.q10.model;

import java.io.Serializable;


public class CustomerRec implements Serializable {
    private boolean insert;
    private int     custKey;
    private String  name;
    private double  acctBal;
    private int     nationKey;
    private String  address;
    private String  phone;
    private String  comment;

    public CustomerRec() {}

    public CustomerRec(boolean insert,
                       int custKey,
                       String name,
                       double acctBal,
                       int nationKey,
                       String address,
                       String phone,
                       String comment){
        this.insert    = insert;
        this.custKey   = custKey;
        this.name      = name;
        this.acctBal   = acctBal;
        this.nationKey = nationKey;
        this.address   = address;
        this.phone     = phone;
        this.comment   = comment;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public String getName(){ return name; }
    public void   setName(String v){ name = v; }

    public double getAcctBal(){ return acctBal; }
    public void   setAcctBal(double v){ acctBal = v; }

    public int getNationKey(){ return nationKey; }
    public void setNationKey(int v){ nationKey = v; }

    public String getAddress(){ return address; }
    public void   setAddress(String v){ address = v; }

    public String getPhone(){ return phone; }
    public void   setPhone(String v){ phone = v; }

    public String getComment(){ return comment; }
    public void   setComment(String v){ comment = v; }

    @Override public String toString(){
        return "CustomerRec{" +
                "ins=" + insert +
                ", ckey=" + custKey +
                ", name='" + name + '\'' +
                ", bal=" + acctBal +
                ", nkey=" + nationKey +
                '}';
    }
}
