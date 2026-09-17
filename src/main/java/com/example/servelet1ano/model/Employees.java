package com.example.servelet1ano.model;

import java.util.Date;

public class Employees {
    private int id;
    private int companyId;
    private int permissionGroupId;
    private int unitId;
    private PermissionGroups permissionGroup;
    private String email;
    private String name;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private Companies company;
    private Units unit;


    public Employees(int id, int companyId, int permissionGroupId, PermissionGroups permissionGroup, String email, String name, Date createdAt, Companies company) {
        this.id = id;
        this.companyId = companyId;
        this.permissionGroupId = permissionGroupId;
        this.permissionGroup = permissionGroup;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
        this.company = company;
    }

    public Employees() {
    }

    public Employees(String name, String email) {
        this.email = email;
        this.name = name;
    }

    public Employees(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Employees(int id, String name, String email, PermissionGroups permissionGroup, Companies company) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.permissionGroup = permissionGroup;
        this.company = company;
    }

    public Employees(int id, int companyId, int permissionGroupId, int unitId, String email, String name, PermissionGroups permissionGroup, Companies company, Units unit) {
        this.id = id;
        this.companyId = companyId;
        this.permissionGroupId = permissionGroupId;
        this.unitId = unitId;
        this.permissionGroup = permissionGroup;
        this.email = email;
        this.name = name;
        this.company = company;
        this.unit = unit;
    }

    public Employees(int companyId, int permissionGroupId, String email, String name) {
        this.companyId = companyId;
        this.permissionGroupId = permissionGroupId;
        this.email = email;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getPermissionGroupId() {
        return permissionGroupId;
    }

    public void setPermissionGroupId(int permissionGroupId) {
        this.permissionGroupId = permissionGroupId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public PermissionGroups getPermissionGroup() {
        return permissionGroup;
    }

    public void setPermissionGroup(PermissionGroups permissionGroup) {
        this.permissionGroup = permissionGroup;
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
