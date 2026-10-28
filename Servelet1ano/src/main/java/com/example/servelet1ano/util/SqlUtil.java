package com.example.servelet1ano.util;

/**
 * Utilitarios para montar consultas SQL com seguranca
 */
public final class SqlUtil {

    private SqlUtil() {
    }

    /**
     * Escapa os caracteres especiais do LIKE/ILIKE (%, _ e \) para que o texto digitado
     * seja procurado literalmente, e nao interpretado como curinga.
     * O PostgreSQL usa a barra invertida como caractere de escape padrao no LIKE.
     * @param value O texto digitado pelo usuario
     * @return O texto com os curingas escapados (ou null, se o valor for null)
     */
    public static String escapeLike(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
