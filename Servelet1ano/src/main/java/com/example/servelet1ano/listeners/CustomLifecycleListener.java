package com.example.servelet1ano.listeners;

import com.example.servelet1ano.connection.ConnectionFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class CustomLifecycleListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce){
        ConnectionFactory.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce){
        ConnectionFactory.shutdown();
    }
}
