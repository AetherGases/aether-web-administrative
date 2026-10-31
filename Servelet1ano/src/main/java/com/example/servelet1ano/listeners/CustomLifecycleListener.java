package com.example.servelet1ano.listeners;

import com.example.servelet1ano.connection.ConnectionFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Classe que acompanha o inicio e o fim da aplicacao web
 * Abre o pool de conexoes com o banco quando a aplicacao sobe e fecha quando ela para
 */
@WebListener
public class CustomLifecycleListener implements ServletContextListener {

    /**
     * metodo chamado pelo servidor quando a aplicacao inicia
     * Inicia o pool de conexoes com o banco
     * @param sce O evento do contexto da aplicacao
     */
    @Override
    public void contextInitialized(ServletContextEvent sce){
        ConnectionFactory.start();
    }

    /**
     * metodo chamado pelo servidor quando a aplicacao para
     * Fecha o pool de conexoes com o banco
     * @param sce O evento do contexto da aplicacao
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce){
        ConnectionFactory.shutdown();
    }
}