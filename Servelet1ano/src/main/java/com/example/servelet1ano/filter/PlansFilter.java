package com.example.servelet1ano.filter;

import java.math.BigDecimal;

public class PlansFilter {
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public PlansFilter(){}

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}