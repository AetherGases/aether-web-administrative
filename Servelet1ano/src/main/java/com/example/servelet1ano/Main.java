package com.example.servelet1ano;

import com.example.servelet1ano.connection.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main() {
        //Testando conexao

        System.out.println("Testando a conexão");

        try (Connection conn = ConnectionFactory.connect()){
            System.out.println("conexao com sucesso");

        } catch (SQLException e) {

            System.out.println("conexao com erro");

            System.out.println(e.getMessage());;
        }
    }
}
