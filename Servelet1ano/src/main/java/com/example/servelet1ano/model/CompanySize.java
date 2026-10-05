package com.example.servelet1ano.model;

public enum CompanySize {
    PEQUENO("pequeno"),
    MEDIO("medio"),
    GRANDE("grande");

    private String valor;

    CompanySize(String valor){
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    /**
     * Converte o valor salvo no banco de volta pro enum correspondente
     * @param valor O valor como esta salvo na coluna size
     * @return O CompanySize correspondente
     */
    public static CompanySize fromValor(String valor) {
        for (CompanySize size : values()) {
            if (size.valor.equalsIgnoreCase(valor)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Porte invalido: " + valor);
    }
}