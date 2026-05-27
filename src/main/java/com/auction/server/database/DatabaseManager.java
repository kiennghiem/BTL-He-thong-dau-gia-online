package com.auction.server.database; // Khớp gói hệ thống của Server

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Hệ thống Quản lý Connection Pool tập trung (HikariCP).
 * Thiết kế chuẩn hóa: Kết hợp giữa đọc file cấu hình động và mẫu Singleton Pattern
 * để khớp hoàn toàn với cấu trúc gọi tài nguyên của BaseDAO.
 */
public final class DatabaseManager {

    private static DatabaseManager instance;
    private HikariDataSource dataSource;

    // Các thông số cấu hình dự phòng (Fallback) nếu không đọc được file properties
    private String url = "jdbc:mysql://localhost:3306/auction_db?useSSL=false&allowPublicKeyRetrieval=true";
    private String username = "root";
    private String password = "Seductivemonke";
    private int poolSize = 10;
    private int connectionTimeout = 30000;
    private int idleTimeout = 600000;
    private int maxLifetime = 1800000;

    /**
     * Khởi tạo Private Constructor để đảm bảo tính đóng gói của Singleton.
     */
    private DatabaseManager() {
        loadConfig();
        initDataSource();
    }

    /**
     * Điểm truy cập toàn cục duy nhất cho ứng dụng Server (Thread-safe).
     * Giúp BaseDAO có thể gọi dễ dàng.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * 1. Đọc cấu hình động từ file tài nguyên database.properties
     */
    private void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.err.println("[DB-Manager] Không tìm thấy database.properties. Sử dụng cấu hình mặc định.");
                return;
            }
            properties.load(input);

            this.url = properties.getProperty("db.url", this.url);
            this.username = properties.getProperty("db.username", this.username);
            this.password = properties.getProperty("db.password", this.password);
            this.poolSize = Integer.parseInt(properties.getProperty("db.pool.size", String.valueOf(this.poolSize)));

            System.out.println("[DB-Manager] Tải file cấu hình database.properties thành công.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("[DB-Manager] Lỗi đọc file cấu hình, chuyển sang cấu hình dự phòng: " + e.getMessage());
        }
    }

    /**
     * 2. Khởi tạo cấu hình kết nối tối ưu cho HikariCP & MySQL
     */
    private void initDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            // Cấu hình lõi
            config.setJdbcUrl(this.url);
            config.setUsername(this.username);
            config.setPassword(this.password);

            // Cấu hình Pool Tuning
            config.setMaximumPoolSize(this.poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(this.connectionTimeout);
            config.setIdleTimeout(this.idleTimeout);
            config.setMaxLifetime(this.maxLifetime);

            // Tối ưu hóa hiệu năng tầng Driver cho hệ thống Đấu giá (Chống nghẽn lệnh)
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            this.dataSource = new HikariDataSource(config);
            System.out.println("[DB-Manager] Khởi tạo HikariCP Connection Pool thành công.");
        } catch (Exception e) {
            System.err.println("[DB-Manager] Lỗi nghiêm trọng khi tạo DataSource: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Cấp phát một kết nối an toàn từ Pool.
     * @return Connection thực thi SQL
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource đã đóng hoặc chưa được khởi tạo.");
        }
        return dataSource.getConnection();
    }

    // =========================================================================
    // TIỆN ÍCH QUẢN LÝ TRANSACTION CHUYÊN SÂU (Nếu cần dùng ở Service)
    // =========================================================================

    public void beginTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.setAutoCommit(false);
        }
    }

    public void commitTransaction(Connection conn) throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollbackTransaction(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("[DB-Manager] Rollback thất bại: " + e.getMessage());
            }
        }
    }

    /**
     * Kiểm tra sức khỏe của Pool định kỳ.
     */
    public boolean healthCheck() {
        if (dataSource == null || dataSource.isClosed()) return false;
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đóng an toàn toàn bộ Pool khi tắt ứng dụng Server.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DB-Manager] Đã đóng Connection Pool an toàn.");
        }
    }
}