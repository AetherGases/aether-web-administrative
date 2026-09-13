package com.example.servelet1ano.model;

import java.util.Date;

public class Sectors {
    private int id;
    private int unitId;
    private int companyId;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private Companies company;
    private Units unit;

    public Sectors(int id, int unitId, int companyId, String description, Date createdAt) {
        this.id = id;
        this.unitId = unitId;
        this.companyId = companyId;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Sectors(String description) {
        this.description = description;
    }

    public Sectors() {
    }

    public Sectors(int id, int unitId, int companyId, String description, Companies company, Units unit) {
        this.id = id;
        this.unitId = unitId;
        this.companyId = companyId;
        this.description = description;
        this.company = company;
        this.unit = unit;
    }

    //    Getters e setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }

    public Units getUnit() {
        return unit;
    }

    public void setUnit(Units unit) {
        this.unit = unit;
    }
}
