package com.example.servelet1ano.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    /**
     * Abre a conexão com o banco usando os dados do .env
     * @return A conexão aberta (nunca retorna null)
     * @throws IllegalStateException se as configurações do banco estiverem ausentes
     * @throws RuntimeException se o driver não for encontrado ou não for possível conectar
     */
    public static Connection connect(){

        String url = DatabaseConfig.getUrl();
        String user = DatabaseConfig.getUser();
        String password = DatabaseConfig.getPassword();

        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new IllegalStateException(
                    "As configurações do banco de dados não foram encontradas. "
                            + "Preencha DB_URL, DB_USER e DB_PASSWORD no arquivo .env."
            );
        }

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException e){
            throw new RuntimeException(
                    "O driver do PostgreSQL não foi encontrado. "
                            + "Verifique se a dependência do postgresql está declarada no pom.xml.",
                    e
            );
        }
        catch (SQLException e){
            throw new RuntimeException(
                    "Não foi possível conectar ao banco de dados. "
                            + "Verifique se o servidor está em execução e se o usuário e a senha do arquivo .env estão corretos.",
                    e
            );
        }
    }

    /**
     * Fecha a conexão com o banco
     * @param conn A conexão que será fechada
     */
    public static void disconnect(Connection conn){
        try {
            if (conn != null && !conn.isClosed()){
                conn.close();
            }
        }catch (SQLException e){
            System.out.println("Erro: não foi possível fechar a conexão com o banco de dados.");
            e.printStackTrace();
        }
    }
}