package com.example.servelet1ano.connection;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Classe que le as configuracoes do banco no arquivo .env
 * Se o .env nao existir ou estiver com erro, os metodos devolvem null
 */
public class DatabaseConfig {

    private static final Dotenv dotenv = load();

    /**
     * Carrega o arquivo .env sem derrubar a aplicacao caso ele nao exista
     * Se o arquivo faltar, tenta usar as variaveis de ambiente do sistema
     * @return O Dotenv carregado ou null se nao foi possivel ler o arquivo
     */
    private static Dotenv load() {
        try {
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (DotenvException e) {
            System.err.println("Error: could not read the .env file.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pega uma chave do .env conferindo antes se o arquivo foi carregado
     * @param key O nome da variavel no .env
     * @return O valor da variavel ou null se nao foi encontrada
     */
    private static String get(String key) {
        if (dotenv == null) {
            return null;
        }
        return dotenv.get(key);
    }

    public static String getUrl() {
        return get("DB_URL");
    }

    public static String getUser() {
        return get("DB_USER");
    }

    public static String getPassword() {
        return get("DB_PASSWORD");
    }
}