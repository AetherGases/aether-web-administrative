package com.example.servelet1ano.model;

public enum StatusEmployee {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    IN_VACATION("IN_VACATION");

    private final String valor;

    StatusEmployee(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
