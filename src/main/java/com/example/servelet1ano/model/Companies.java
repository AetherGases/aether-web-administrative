package com.example.servelet1ano.model;

import java.sql.Date;

public class Companies {
    private int id;
    private int addressId;
    private String name;
    private int size;
    private Date registrationDate;
    private String taxId;
    private String email;
    private Date createdAt;
    private Date updatedAt;
    private Addresses address;

    public Companies(int id, int addressId, String name, int size, Date registrationDate, String taxId, String email, Date createdAt) {
        this.id = id;
        this.addressId = addressId;
        this.name = name;
        this.size = size;
        this.registrationDate = registrationDate;
        this.taxId = taxId;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Companies(String name, int size, String taxId, String email) {
        this.name = name;
        this.size = size;
        this.taxId = taxId;
        this.email = email;
    }

    public Companies() {
    }

    public Companies(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Companies(int id, String name, int size, String taxId, String email) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.taxId = taxId;
        this.email = email;
    }

    public Companies(int id, int addressId, String name, int size, Date registrationDate, String taxId, String email, Date createdAt, Addresses address) {
        this.id = id;
        this.addressId = addressId;
        this.name = name;
        this.size = size;
        this.registrationDate = registrationDate;
        this.taxId = taxId;
        this.email = email;
        this.createdAt = createdAt;
        this.address = address;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
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

    public Addresses getAddress() {
        return address;
    }

    public void setAddress(Addresses address) {
        this.address = address;
    }
}
