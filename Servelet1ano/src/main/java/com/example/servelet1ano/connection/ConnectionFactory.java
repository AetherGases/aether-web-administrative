package com.example.servelet1ano.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe que cria e entrega as conexoes com o banco usando um pool de conexoes (HikariCP)
 * O pool precisa ser iniciado com start() quando a aplicacao sobe e fechado com shutdown() quando ela para
 */
public class ConnectionFactory {

    /**
     * Pool de conexoes com o banco, criado no start() e fechado no shutdown()
     */
    private static HikariDataSource dataSource;

    /**
     * metodo que inicia o pool de conexoes usando os dados do .env
     * Deve ser chamado uma vez, quando a aplicacao inicia (feito pelo CustomLifecycleListener)
     * @throws IllegalStateException se DB_URL, DB_USER ou DB_PASSWORD estiverem ausentes no .env
     * @throws RuntimeException se o driver do PostgreSQL nao for encontrado
     */
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
     * metodo que entrega uma conexao do pool
     * Quem pedir a conexao precisa fechar (de preferencia com try-with-resources), o que a devolve ao pool
     * Antes de usar, o pool precisa ter sido iniciado com start()
     * @return A conexao aberta com o banco
     * @throws SQLException se nao for possivel obter a conexao do pool
     */
    public static Connection connect() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * metodo que fecha o pool de conexoes
     * Deve ser chamado quando a aplicacao para (feito pelo CustomLifecycleListener)
     * Se o pool nao foi iniciado ou ja esta fechado, nao faz nada
     */
    public static void shutdown() {
        if(dataSource != null && !dataSource.isClosed()){
            dataSource.close();
        }
    }
}