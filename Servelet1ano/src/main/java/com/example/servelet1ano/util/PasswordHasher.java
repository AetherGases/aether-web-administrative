package com.example.servelet1ano.util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

/**
 * Classe utilitaria para gerar e conferir hash de senha usando Argon2id
 * O salt é gerado automaticamente e fica embutido no proprio hash
 */
public class PasswordHasher {

    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    private static final int ITERATIONS = 2;      // numero de passagens pela memoria
    private static final int MEMORY_KIB = 19456;  // 19 MiB de memoria
    private static final int PARALLELISM = 1;      // linhas de calculo em paralelo

    /**
     * Gera o hash da senha pronto pra guardar no banco
     * @param password A senha em texto puro
     * @return O hash da senha
     */
    public static String hash(String password) {

        char[] passwordChars = password.toCharArray();

        try {
            return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }

    /**
     * Confere se a senha digitada bate com o hash guardado
     * @param hash O hash que esta no banco
     * @param password A senha digitada no login
     * @return true se a senha confere e false caso contrario
     */
    public static boolean verify(String hash, String password) {

        char[] passwordChars = password.toCharArray();

        try {
            return argon2.verify(hash, passwordChars);
        } finally {
            argon2.wipeArray(passwordChars);
        }
    }
}