package com.example.servelet1ano.model;

public class PermissionGroups {
    private int id;
    private String name;
    private Integer companyId;

    public PermissionGroups(int id, String name, Integer companyId) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
    }

    public PermissionGroups(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public PermissionGroups() {
    }

    public PermissionGroups(String name) {
        this.name = name;
    }

    public PermissionGroups(String name, Integer companyId) {
        this.name = name;
        this.companyId = companyId;
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

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return "PermissionGroups{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", companyId=" + companyId +
                '}';
    }
}
