package main.java.com.auction.server.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnection {
    private static DBConnection instance;
    private HikariDataSource dataSource;

    private DBConnection() {
        HikariConfig config = new HikariConfig();

        // 1. Core Database Configuration
        config.setJdbcUrl("jdbc:mysql://localhost:3306/auction_db");
        config.setUsername("root");
        config.setPassword("Seductivemonke");

        // 2. Connection Pool Tuning
        // Maximum number of connections Hikari will keep in the pool
        config.setMaximumPoolSize(10);
        // Minimum number of idle connections maintained
        config.setMinimumIdle(2);
        // How long a thread will wait for a connection before throwing an exception (30 seconds)
        config.setConnectionTimeout(30000);
        // How long a connection is allowed to sit idle in the pool (10 minutes)
        config.setIdleTimeout(600000);
        // Maximum lifetime of a connection in the pool (30 minutes)
        config.setMaxLifetime(1800000);

        // 3. Performance Optimizations (Highly recommended for MySQL)
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        this.dataSource = new HikariDataSource(config);
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    // Hands out a live connection from the pool
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Call this only when the entire Server application shuts down
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}