package com.example.servelet1ano.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Classe utilitaria para gerar e conferir hash de senha usando Argon2id
 * O salt é gerado automaticamente e fica embutido no proprio hash
 */
public class PasswordHasher {

    // variante Argon2id (hibrida: protege contra GPU e contra side-channel)
    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    // parametros minimos recomendados pelo OWASP
    private static final int ITERACOES = 2;        // numero de passagens pela memoria
    private static final int MEMORIA_KIB = 19456;  // 19 MiB de memoria
    private static final int PARALELISMO = 1;       // linhas de calculo em paralelo

    /**
     * Gera o hash da senha (com salt embutido) pronto pra guardar no banco
     * @param senha A senha em texto puro
     * @return O hash da senha
     */
    public static String hash(String senha) {

        char[] senhaChars = senha.toCharArray();

        try {
            return argon2.hash(ITERACOES, MEMORIA_KIB, PARALELISMO, senhaChars);
        } finally {
            argon2.wipeArray(senhaChars);
        }
    }

    /**
     * Confere se a senha digitada bate com o hash guardado
     * @param hash O hash que esta no banco
     * @param senha A senha digitada no login
     * @return true se a senha confere e false caso contrario
     */
    public static boolean verify(String hash, String senha) {

        char[] senhaChars = senha.toCharArray();

        try {
            return argon2.verify(hash, senhaChars);
        } finally {
            argon2.wipeArray(senhaChars);
        }
    }
}