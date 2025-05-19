package org.example.q10.model;

import java.io.Serializable;


public class JoinCustNationRec implements Serializable {
    private boolean insert;
    private int     custKey;
    private int     orderKey;
    private String  custName;
    private double  acctBal;
    private String  nation;
    private String  address;
    private String  phone;
    private String  comment;
    private double  revenue;

    public JoinCustNationRec(){}

    public JoinCustNationRec(boolean insert, int custKey, int orderKey,
                             String custName, double acctBal, String nation,
                             String address, String phone, String comment,
                             double revenue){
        this.insert   = insert;
        this.custKey  = custKey;
        this.orderKey = orderKey;
        this.custName = custName;
        this.acctBal  = acctBal;
        this.nation   = nation;
        this.address  = address;
        this.phone    = phone;
        this.comment  = comment;
        this.revenue  = revenue;
    }

    public boolean isInsert(){ return insert; }
    public void    setInsert(boolean v){ insert = v; }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public int getOrderKey(){ return orderKey; }
    public void setOrderKey(int v){ orderKey = v; }

    public String getCustName(){ return custName; }
    public void   setCustName(String v){ custName = v; }

    public double getAcctBal(){ return acctBal; }
    public void   setAcctBal(double v){ acctBal = v; }

    public String getNation(){ return nation; }
    public void   setNation(String v){ nation = v; }

    public String getAddress(){ return address; }
    public void   setAddress(String v){ address = v; }

    public String getPhone(){ return phone; }
    public void   setPhone(String v){ phone = v; }

    public String getComment(){ return comment; }
    public void   setComment(String v){ comment = v; }

    public double getRevenue(){ return revenue; }
    public void   setRevenue(double v){ revenue = v; }

    @Override public String toString(){
        return "JoinCustNation{" +
                "ins=" + insert +
                ", ckey=" + custKey +
                ", okey=" + orderKey +
                ", rev=" + revenue +
                '}';
    }
}
