package com.example.servelet1ano.model;

import java.util.Date;

public class PermissionGroups {
    private int id;
    private String name;
    private int companyId;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Companies company;

    public PermissionGroups(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public PermissionGroups() {
    }

    public PermissionGroups(String name) {
        this.name = name;
    }

    public PermissionGroups(int id, String name, int companyId, boolean isActive, Date createdAt, Date updatedAt, Companies company) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.company = company;
    }

    //    Getters e setters

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

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }
}
