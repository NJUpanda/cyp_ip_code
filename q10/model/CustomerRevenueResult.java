package org.example.q10.model;

import java.io.Serializable;


public class CustomerRevenueResult implements Serializable {
    private int    custKey;
    private String custName;
    private double revenue;
    private double acctBal;
    private String nation;
    private String address;
    private String phone;
    private String comment;

    public CustomerRevenueResult(){}

    public CustomerRevenueResult(int custKey, String custName, double revenue, double acctBal,
                                 String nation, String address, String phone, String comment){
        this.custKey  = custKey;
        this.custName = custName;
        this.revenue  = revenue;
        this.acctBal  = acctBal;
        this.nation   = nation;
        this.address  = address;
        this.phone    = phone;
        this.comment  = comment;
    }

    public int getCustKey(){ return custKey; }
    public void setCustKey(int v){ custKey = v; }

    public String getCustName(){ return custName; }
    public void   setCustName(String v){ custName = v; }

    public double getRevenue(){ return revenue; }
    public void   setRevenue(double v){ revenue = v; }

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

    @Override public String toString(){
        return "CustomerRevenueResult{" +
                "ckey=" + custKey +
                ", name='" + custName + '\'' +
                ", revenue=" + revenue +
                ", bal=" + acctBal +
                ", nation='" + nation + '\'' +
                '}';
    }
}
