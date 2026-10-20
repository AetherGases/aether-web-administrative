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
     * Se o valor vier vazio ou nao existir no enum, avisa o problema e devolve null
     * (assim uma linha com status errado nao derruba a listagem inteira)
     * @param valor O valor como esta salvo na coluna status
     * @return O StatusEmployee correspondente ou null se o valor for invalido
     */
    public static StatusEmployee fromValor(String valor) {

        if (valor == null || valor.isBlank()) {
            System.out.println("Error: the employee status is empty. Check the status column in the employees table.");
            return null;
        }

        for (StatusEmployee status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }

        System.out.println("Error: the employee status '" + valor + "' is not valid. The accepted values are ACTIVE, INACTIVE and IN_VACATION.");
        return null;
    }

}