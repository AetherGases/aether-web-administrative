package com.example.servelet1ano.model;

public enum StatusEmployee {
    ACTIVE ("active"),
    ON_LEAVE("on leave"),
    ON_VACATION("on vacation"),
    DISMISSED ("dismissed");

    private String valor;

    StatusEmployee(String valor){
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
