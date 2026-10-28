package com.example.servelet1ano.model;

public enum CompanySize {
    PEQUENA("pequeno"),
    MEDIA("medio"),
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
     * Se o valor vier vazio ou nao existir no enum, avisa o problema e devolve null
     * (assim uma linha com porte errado nao derruba a listagem inteira)
     * @param valor O valor como esta salvo na coluna size
     * @return O CompanySize correspondente ou null se o valor for invalido
     */
    public static CompanySize fromValor(String valor) {

        if (valor == null || valor.isBlank()) {
            System.out.println("Erro: o porte da empresa está vazio. Confira a coluna size da tabela companies.");
            return null;
        }

        for (CompanySize size : values()) {
            if (size.valor.equalsIgnoreCase(valor)) {
                return size;
            }
        }

        System.out.println("Erro: o porte da empresa '" + valor + "' não é válido. Os valores aceitos são pequeno, medio e grande.");
        return null;
    }
}