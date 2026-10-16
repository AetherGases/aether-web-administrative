package com.example.servelet1ano.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    /**
     * Abre a conexao com o banco usando os dados do .env
     * @return A conexao aberta ou null se nao foi possivel conectar
     */
    public static Connection connect(){

        String url = DatabaseConfig.getUrl();
        String user = DatabaseConfig.getUser();
        String password = DatabaseConfig.getPassword();

        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            System.out.println("Error: the database settings are missing. Fill DB_URL, DB_USER and DB_PASSWORD in the .env file.");
            return null;
        }

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;
        }
        catch (ClassNotFoundException e){
            System.out.println("Error: the PostgreSQL driver was not found. Check if the postgresql dependency is declared in the pom.xml file.");
            e.printStackTrace();
            return null;
        }
        catch (SQLException e){
            System.out.println("Error: could not connect to the database. Check if the server is running and if the user and the password in the .env file are correct.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fecha a conexao com o banco
     * @param conn A conexao que sera fechada
     */
    public static void disconnect(Connection conn){
        try {
            if (conn != null && !conn.isClosed()){
                conn.close();
            }
        }catch (SQLException e){
            System.out.println("Error: could not close the database connection.");
            e.printStackTrace();
        }
    }
}