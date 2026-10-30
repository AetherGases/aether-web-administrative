package com.example.servelet1ano.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static HikariDataSource dataSource;

    public static void start(){
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
        }
        catch (ClassNotFoundException e){
            throw new RuntimeException(
                    "O driver do PostgreSQL não foi encontrado. "
                            + "Verifique se a dependência do postgresql está declarada no pom.xml.",
                    e
            );
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(3);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(180000);
        config.setAutoCommit(true);
        config.setConnectionTestQuery("select 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Abre a conexão com o banco usando os dados do .env
     * @return A conexão aberta (nunca retorna null)
     * @throws IllegalStateException se as configurações do banco estiverem ausentes
     * @throws RuntimeException se o driver não for encontrado ou não for possível conectar
     */
    public static Connection connect() throws SQLException {
        return dataSource.getConnection();
    }

    public static void shutdown() {
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}