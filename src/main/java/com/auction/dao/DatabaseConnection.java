package main.java.com.auction.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class to manage the database connection.
 * Ensures only one connection instance is used across the Server.
 */
public class DatabaseConnection {
    // Static instance for Singleton pattern
    private static DatabaseConnection instance;
    private Connection connection;

    // Database configuration - Update these details for your local setup
    private final String url = "jdbc:mysql://localhost:3306/auction_db";
    private final String username = "root";
    private final String password = "your_password";

    // Private constructor to prevent instantiation from other classes
    private DatabaseConnection() throws SQLException {
        try {
            // Load the driver (Optional for modern JDBC but recommended)
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database Driver not found.", e);
        }
    }

    /**
     * Provides the global point of access to the connection instance.
     * Uses double-checked locking for thread safety.
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        } else if (instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the connection when the server shuts down[cite: 55].
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
    }
}