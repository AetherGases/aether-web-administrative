package com.example.servelet1ano.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    public static Connection connect(){

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection("jdbc:postgresql://pg-af463ee-laisalmeida0143-375e.j.aivencloud.com:24903/db_aether?sslmode=require", "avnadmin", " ");

            return conn;
        }
        catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
            return null;
        }
    }


    public static void disconnect(Connection conn){
        try {
            if (conn != null || !conn.isClosed()){
                conn.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}