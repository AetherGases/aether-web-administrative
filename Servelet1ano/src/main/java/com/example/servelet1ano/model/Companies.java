package com.example.servelet1ano.model;

import java.sql.Date;

public class Companies {
    private int id;
    private int addressId;
    private String name;
    private String tradeName;
    private String cnpj;
    private CompanySize size;
    private Date registrationDate;
    private String email;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private Addresses address;

    public Companies(int id, int addressId, String name, CompanySize size, Date registrationDate, String cnpj, String email, Date createdAt) {
        this.id = id;
        this.addressId = addressId;
        this.name = name;
        this.size = size;
        this.registrationDate = registrationDate;
        this.cnpj = cnpj;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Companies(String name, CompanySize size, String cnpj, String email) {
        this.name = name;
        this.size = size;
        this.cnpj = cnpj;
        this.email = email;
    }

    public Companies(int id, String name, CompanySize size, String cnpj, String email) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.cnpj = cnpj;
        this.email = email;
    }

    public Companies(int id, int addressId, String name, String tradeName, String cnpj, CompanySize size, Date registrationDate, String email, Date createdAt, boolean isActive, Addresses address) {
        this.id = id;
        this.addressId = addressId;
        this.name = name;
        this.tradeName = tradeName;
        this.cnpj = cnpj;
        this.size = size;
        this.registrationDate = registrationDate;
        this.email = email;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.address = address;
    }

    public Companies() {
    }

    public Companies(int id, String name) {
        this.id = id;
        this.name = name;
    }

    //    Getters e setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public CompanySize getSize() {
        return size;
    }

    public void setSize(CompanySize size) {
        this.size = size;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Addresses getAddress() {
        return address;
    }

    public void setAddress(Addresses address) {
        this.address = address;
    }
}
