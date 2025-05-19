package org.example.q14.model;

import java.io.Serializable;


public class PromoRevenueResult implements Serializable {

    private double promoRevenuePct;

    public PromoRevenueResult() {}

    public PromoRevenueResult(double promoRevenuePct) {
        this.promoRevenuePct = promoRevenuePct;
    }

    public double getPromoRevenuePct() {
        return promoRevenuePct;
    }

    public void setPromoRevenuePct(double promoRevenuePct) {
        this.promoRevenuePct = promoRevenuePct;
    }

    @Override
    public String toString() {
        return "PromoRevenueResult{" +
                "promoRevenuePct=" + promoRevenuePct +
                '%';
    }
}
