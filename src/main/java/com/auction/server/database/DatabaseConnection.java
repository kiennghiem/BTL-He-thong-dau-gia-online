package com.auction.server.database;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for managing database connections using a HikariCP connection pool.
 * This class provides a centralized way to get and manage database connections efficiently.
 * Applying Singleton design pattern using the static block to ensure the connection pool is created only once
 * when the app starts.
 * It is thread-safe and designed to be used throughout the server application.
 */
public final class DatabaseConnection2 {
    private static HikariDataSource dataSource;
    private static HikariConfig config = new HikariConfig();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseConnection2() {}

    // Constructing a DataSource (connection pool) - this block always runs ONCE when starting the app.
    static {
        try {
            config.setJdbcUrl("jdbc:mysql://localhost:3306/test");
            config.setUsername("root");
            config.setPassword("");
            // Maximum time for a thread waiting for a connection from the connection pool is 30000ms (30sec),
            // If this time is exceeded without a connection becoming available, a SQLException will be thrown.
            config.setConnectionTimeout(30000);
            // Maximum time that a connection is allowed to sit idle (unused) in the connection pool.
            config.setIdleTimeout(60000);
            // Maximum lifetime of a connection in the pool.
            config.setMaxLifetime(1800000);
            // Maximum number of connections allowed in the pool. When the pool reaches this size, and no idle connections
            // are available, calls to getConnection() will block for up to 'connectionTimeout' milliseconds before timing out.
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
        catch (Exception e) {
            throw new RuntimeException("Error initializing database pool", e);
        }
    }

    /**
     * Retrieves a database connection from the connection pool.
     *
     * @return A {@link Connection} object from the pool.
     * @throws SQLException If a database access error occurs or a connection cannot be obtained.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Shuts down the connection pool and releases all resources.
     * This method should be called when the application is closing.
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }

}
