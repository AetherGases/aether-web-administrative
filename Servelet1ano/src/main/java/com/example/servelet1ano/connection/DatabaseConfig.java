package com.example.servelet1ano.connection;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Classe que le as configuracoes do banco no arquivo .env
 * Se o .env nao existir ou estiver com erro, os metodos devolvem null
 */
public class DatabaseConfig {

    /**
     * Nome da variavel de ambiente (ou propriedade -D da JVM) com a pasta onde esta o .env
     * Util quando a aplicacao roda no Tomcat, porque a pasta de trabalho dele nao e a do projeto
     * Se nao for informada, o dotenv procura na pasta de trabalho e depois no classpath
     */
    private static final String ENV_DIRECTORY = "DOTENV_DIR";

    /**
     * Conteudo do arquivo .env carregado uma unica vez ao iniciar a classe
     * Fica null se o arquivo nao pode ser lido
     */
    private static final Dotenv dotenv = load();

    /**
     * Carrega o arquivo .env sem derrubar a aplicacao caso ele nao exista
     * Se o arquivo faltar, tenta usar as variaveis de ambiente do sistema
     * @return O Dotenv carregado ou null se nao foi possivel ler o arquivo
     */
    private static Dotenv load() {
        try {
            var builder = Dotenv.configure().ignoreIfMissing();

            String directory = System.getProperty(ENV_DIRECTORY);

            if (directory == null || directory.isBlank()) {
                directory = System.getenv(ENV_DIRECTORY);
            }

            if (directory != null && !directory.isBlank()) {
                builder = builder.directory(directory.trim());
            }

            return builder.load();
        } catch (DotenvException e) {
            System.err.println("Erro: não foi possível ler o arquivo .env.");
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
        String value = null;

        if (dotenv != null) {
            value = dotenv.get(key);
        }

        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }

        return value;
    }

    /**
     * metodo que busca a url de conexao com o banco (variavel DB_URL)
     * @return A url do banco ou null se nao foi encontrada
     */
    public static String getUrl() {
        return get("DB_URL");
    }

    /**
     * metodo que busca o usuario do banco (variavel DB_USER)
     * @return O usuario do banco ou null se nao foi encontrado
     */
    public static String getUser() {
        return get("DB_USER");
    }

    /**
     * metodo que busca a senha do banco (variavel DB_PASSWORD)
     * @return A senha do banco ou null se nao foi encontrada
     */
    public static String getPassword() {
        return get("DB_PASSWORD");
    }

    /**
     * metodo que busca a chave da API da Brevo, usada no envio de e-mails (variavel BREVO_API_KEY)
     * @return A chave da API ou null se nao foi encontrada
     */
    public static String getBrevoApiKey() {
        return get("BREVO_API_KEY");
    }

    /**
     * metodo que busca o e-mail remetente dos e-mails enviados pela Brevo (variavel BREVO_FROM_EMAIL)
     * @return O e-mail do remetente ou null se nao foi encontrado
     */
    public static String getBrevoFromEmail() {
        return get("BREVO_FROM_EMAIL");
    }

    /**
     * metodo que busca o nome do remetente dos e-mails enviados pela Brevo (variavel BREVO_FROM_NAME)
     * @return O nome do remetente ou null se nao foi encontrado
     */
    public static String getBrevoFromName() {
        return get("BREVO_FROM_NAME");
    }

    /**
     * metodo que busca a chave secreta usada para gerar o hash do codigo de recuperacao de senha (variavel PASSWORD_RECOVERY_SECRET)
     * @return A chave secreta ou null se nao foi encontrada
     */
    public static String getPasswordRecoverySecret() {
        return get("PASSWORD_RECOVERY_SECRET");
    }
}