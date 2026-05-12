package main.java.com.auction.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abstract Base DAO to provide database connection capabilities.
 * This promotes code reuse across UserDAO, ItemDAO, and AuctionDAO.
 */
public abstract class BaseDAO {
    // Database credentials - move these to AppConstants later if possible
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static final String USER = "root";
    private static final String PASS = "password";

    /**
     * Establishes a connection to the database.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    protected Connection getConnection() throws SQLException {
        try {
            // Loading the driver is often necessary in older JDBC versions
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.err.println("Database Driver not found: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    /**
     * Utility method to safely close database resources.
     * This helps prevent "Memory Leaks" and "Connection Exhaustion".
     */
    protected void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }
}