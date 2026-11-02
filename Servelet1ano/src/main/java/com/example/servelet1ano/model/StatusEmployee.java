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
            System.out.println("Erro: o status do funcionário está vazio. Confira a coluna status da tabela employees.");
            return null;
        }

        for (StatusEmployee status : values()) {
            if (status.valor.equalsIgnoreCase(valor)) {
                return status;
            }
        }

        System.out.println("Erro: o status do funcionário '" + valor + "' não é válido. Os valores aceitos são ACTIVE, INACTIVE e IN_VACATION.");
        return null;
    }

}