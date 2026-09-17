package com.example.servelet1ano.model;
import java.util.Date;

public class Units {
    private int id;
    private int companyId;
    private int addressId;
    private String name;
    private String cnpj;
    private String cnae;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Companies company;
    private Addresses address;

    public Units() {
    }

    public Units(int id, int companyId, int addressId, String name, String cnpj, String cnae, boolean isActive, Date createdAt) {
        this.id = id;
        this.companyId = companyId;
        this.addressId = addressId;
        this.name = name;
        this.cnpj = cnpj;
        this.cnae = cnae;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public Units(String name, String cnpj, String cnae, boolean isActive) {
        this.name = name;
        this.cnpj = cnpj;
        this.cnae = cnae;
        this.isActive = isActive;
    }

    public Units(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Units(int id, int companyId, int addressId, String name, String cnpj, String cnae, boolean isActive, Companies company, Addresses address) {
        this.id = id;
        this.companyId = companyId;
        this.addressId = addressId;
        this.name = name;
        this.cnpj = cnpj;
        this.cnae = cnae;
        this.isActive = isActive;
        this.company = company;
        this.address = address;
    }

    //    Getters e setters

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

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getCnae() {
        return cnae;
    }

    public void setCnae(String cnae) {
        this.cnae = cnae;
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

    public Addresses getAddress() {
        return address;
    }

    public void setAddress(Addresses address) {
        this.address = address;
    }
}
