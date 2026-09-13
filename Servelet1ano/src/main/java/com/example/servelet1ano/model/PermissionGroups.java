package com.example.servelet1ano.model;

public class PermissionGroups {
    private int id;
    private String name;

    public PermissionGroups(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public PermissionGroups() {
    }

    public PermissionGroups(String name) {
        this.name = name;
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
}
