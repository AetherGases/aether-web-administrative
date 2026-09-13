package com.example.servelet1ano.connection;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Isso foi a IA que falou para colocar (pq eu pedi pra ela fazer com .env)

public class ConnectionFactory {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = String.format(
            "jdbc:postgresql://%s:%s/%s",
            dotenv.get("DB_HOST"),
            dotenv.get("DB_PORT"),
            dotenv.get("DB_NAME")
    );

    private static final String USUARIO = dotenv.get("DB_USER");
    private static final String SENHA = dotenv.get("DB_PASSWORD");
    private static final String DRIVER = dotenv.get("DB_DRIVER");

    public static Connection connect() throws SQLException {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("Driver PostgreSQL não encontrado");
        }
    }

    public static void main(String[] args) {
        try {
            Connection conn = connect();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexão com sucesso");
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro de conexão: " + e.getMessage());
            e.printStackTrace();
        }
    }
}