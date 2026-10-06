package com.example.servelet1ano.model;

import java.util.Date;

public class Employees {
    private int id;
    private String cpf;
    private int companyId;
    private int permissionGroupId;
    private int unitId;
    private int sectorId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private StatusEmployee status;
    private int storageFileId;
    private Date createdAt;
    private Date updatedAt;
    private PermissionGroups permissionGroup;
    private Companies company;
    private Units unit;
    private Sectors sector;

    public Employees() {
    }

    public Employees(String name, String email) {
        this.name = name;
        this.email = email;
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

    public Employees(int companyId, int permissionGroupId, int sectorId, String cpf, String name, String email, String phone, String passwordHash, StatusEmployee status) {
        this.companyId = companyId;
        this.permissionGroupId = permissionGroupId;
        this.sectorId = sectorId;
        this.cpf = cpf;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    public Employees(int id, int companyId, int permissionGroupId, int unitId, int sectorId, String cpf, String name, String email, String phone, StatusEmployee status, PermissionGroups permissionGroup, Companies company, Units unit, Sectors sector) {
        this.id = id;
        this.companyId = companyId;
        this.permissionGroupId = permissionGroupId;
        this.unitId = unitId;
        this.sectorId = sectorId;
        this.cpf = cpf;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.permissionGroup = permissionGroup;
        this.company = company;
        this.unit = unit;
        this.sector = sector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
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

    public int getSectorId() {
        return sectorId;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public StatusEmployee getStatus() {
        return status;
    }

    public void setStatus(StatusEmployee status) {
        this.status = status;
    }

    public int getStorageFileId() {
        return storageFileId;
    }

    public void setStorageFileId(int storageFileId) {
        this.storageFileId = storageFileId;
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

    public Sectors getSector() {
        return sector;
    }

    public void setSector(Sectors sector) {
        this.sector = sector;
    }

    @Override
    public String toString() {
        return "Employees{" +
                "id=" + id +
                ", cpf='" + cpf + '\'' +
                ", companyId=" + companyId +
                ", permissionGroupId=" + permissionGroupId +
                ", unitId=" + unitId +
                ", sectorId=" + sectorId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", storageFileId=" + storageFileId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", permissionGroup=" + permissionGroup +
                ", company=" + company +
                ", unit=" + unit +
                ", sector=" + sector +
                '}';
    }
}