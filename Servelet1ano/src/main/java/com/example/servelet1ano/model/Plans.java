package com.example.servelet1ano.model;

import java.math.BigDecimal;
import java.util.Date;

public class Plans {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private int durationDays;
    private String currency;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    public Plans(){}

    public Plans(int id, String name, BigDecimal price, int durationDays) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationDays = durationDays;
    }

    /**
     * Mantido para quem ainda cria o plano com double; o valor e convertido para BigDecimal
     */
    public Plans(int id, String name, double price, int durationDays) {
        this(id, name, BigDecimal.valueOf(price), durationDays);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}