package com.example.servelet1ano.model;

public class TelephoneCompanies {
    private int id;
    private String telephone;
    private int companyId;
    private Companies company;

    public TelephoneCompanies(String telephone, int companyId, int id) {
        this.telephone = telephone;
        this.companyId = companyId;
        this.id = id;
    }

    public TelephoneCompanies(String telephone) {
        this.telephone = telephone;
    }

    public TelephoneCompanies() {
    }

    public TelephoneCompanies(String telephone, int companyId, int id, Companies company) {
        this.telephone = telephone;
        this.companyId = companyId;
        this.id = id;
        this.company = company;
    }

    //    Getters e setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public Companies getCompany() {
        return company;
    }

    public void setCompany(Companies company) {
        this.company = company;
    }
}
