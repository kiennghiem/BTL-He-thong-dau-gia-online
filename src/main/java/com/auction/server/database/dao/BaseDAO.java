package com.auction.server.database.dao;

import com.auction.server.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract Base DAO providing centralized database connection access.
 * Routes directly through the centralized HikariCP pool.
 */
public abstract class BaseDAO {
    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);

    /**
     * Obtains an active connection from the HikariCP pool.
     * @return Connection object
     * @throws SQLException if connection pool runs out or fails
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseManager.getInstance().getConnection();
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
                    logger.error("Error returning resource to pool", e);
                }
            }
        }
    }
}