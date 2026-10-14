package com.example.servelet1ano.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    /**
     * Abre a conexao com o banco
     * Se der erro, mostra uma mensagem explicando o motivo e devolve null
     * Quem chamar precisa conferir se veio null antes de usar a conexao
     * @return Connection aberta, ou null caso nao tenha conseguido conectar
     */
    public static Connection connect(){

        String url = DatabaseConfig.getUrl();
        String user = DatabaseConfig.getUser();
        String password = DatabaseConfig.getPassword();

        // confere se o .env trouxe os tres valores antes de tentar conectar
        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            System.out.println("Configuration error: DB_URL, DB_USER and DB_PASSWORD must be filled in the .env file");
            return null;
        }

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException e){
            System.out.println("Error: PostgreSQL driver not found. Check if the dependency is in the pom.xml");
            return null;
        }
        catch (SQLException e){
            System.out.println(connectionMessage(e));
            return null;
        }
    }


    /**
     * Metodo que fecha a conexao
     * Se der erro ao fechar, so avisa (nao derruba o programa)
     * @param conn A conexao que sera fechada
     */
    public static void disconnect(Connection conn){
        try {
            if (conn != null && !conn.isClosed()){
                conn.close();
            }
        }catch (SQLException e){
            System.out.println("Warning: could not close the database connection. Detail: " + e.getMessage());
        }
    }


    /**
     * Traduz o erro de conexao do PostgreSQL (SQLState) numa mensagem facil de entender
     * @param e A excecao que veio do DriverManager
     * @return A mensagem explicando o que provavelmente esta errado
     */
    private static String connectionMessage(SQLException e){

        String code = e.getSQLState();

        if (code == null) {
            return "Error connecting to the database. Check the DB_URL in the .env file. Detail: " + e.getMessage();
        }

        switch (code) {
            case "28P01":
                return "Error: the database password is incorrect. Check the DB_PASSWORD in the .env file";
            case "28000":
                return "Error: the database user is not allowed to connect. Check the DB_USER in the .env file";
            case "3D000":
                return "Error: the database informed in the DB_URL does not exist. Check the database name";
            case "57P03":
                return "Error: the database is starting up or is not accepting connections right now. Try again in a few seconds";
            case "08001":
            case "08006":
                return "Error: could not connect to the database. Check if PostgreSQL is running and if the host and port in the DB_URL are correct";
            default:
                return "Error connecting to the database (code " + code + "). Detail: " + e.getMessage();
        }
    }

}