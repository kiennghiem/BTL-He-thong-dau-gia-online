package main.java.com.auction.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract Base DAO providing centralized database connection access.
 * Routes directly through the centralized HikariCP pool.
 */
public abstract class BaseDAO {

    /**
     * Obtains an active connection from the HikariCP pool.
     * @return Connection object
     * @throws SQLException if connection pool runs out or fails
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Utility method to safely return database resources.
     * With HikariCP, closing the connection returns it to the pool.
     */
    protected void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    System.err.println("Error returning resource to pool: " + e.getMessage());
                }
            }
        }
    }
}