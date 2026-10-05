package com.example.servelet1ano.model;

public enum StatusEmployee {
    ACTIVE ("ACTIVE"),
    INACTIVE ("INACTIVE"),
    IN_VACATION ("IN_VACATION");

    private String valor;

    StatusEmployee(String valor){
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    /**
     * Converte o valor salvo no banco de volta pro enum correspondente
     * @param valor O valor como esta salvo na coluna status
     * @return O StatusEmployee correspondente
     */
    public static StatusEmployee fromValor(String valor) {
        for (StatusEmployee status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status invalido: " + valor);
    }

}