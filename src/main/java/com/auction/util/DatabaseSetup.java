package com.auction.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích hỗ trợ khởi tạo database và tự động chạy schema.sql trong môi trường local/dev.
 * Được cải tiến sử dụng HikariCP để quản lý kết nối an toàn và hiệu năng cao.
 */
public class DatabaseSetup {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetup.class);

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    public DatabaseSetup() {
        loadConfig();
    }

    /**
     * 1. Khởi tạo database nếu chưa có và chạy toàn bộ cấu trúc schema.sql sử dụng HikariCP.
     */
    public void initDatabase() {
        logger.info("[DB-Setup] Đang khởi động quá trình bootstrap database bằng HikariCP...");

        String adminUrl = buildAdminUrl(this.dbUrl);

        // =========================================================================
        // Bước A: Sử dụng HikariCP ngắn hạn kết nối tới cấp Server để tạo Database
        // =========================================================================
        HikariConfig adminConfig = new HikariConfig();
        adminConfig.setJdbcUrl(adminUrl);
        adminConfig.setUsername(this.dbUser);
        adminConfig.setPassword(this.dbPassword);
        // Cấu hình pool siêu nhỏ và thời gian chờ ngắn cho tác vụ khởi tạo nhanh
        adminConfig.setMaximumPoolSize(2);
        adminConfig.setConnectionTimeout(10000);

        String createDbQuery = "CREATE DATABASE IF NOT EXISTS " + this.dbName +
                " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;";

        // try-with-resources đảm bảo HikariDataSource (adminDs) tự động close và hủy pool sau khi xong block
        try (HikariDataSource adminDs = new HikariDataSource(adminConfig);
             Connection adminConn = adminDs.getConnection();
             Statement stmt = adminConn.createStatement()) {

            stmt.executeUpdate(createDbQuery);
            logger.info("[DB-Setup] Kiểm tra/Tạo database thành công: {}", dbName);

        } catch (SQLException e) {
            logger.error("[DB-Setup] Lỗi khi tạo database ở cấp Server", e);
            return;
        }

        // =========================================================================
        // Bước B: Sử dụng HikariCP ngắn hạn kết nối trực tiếp vào DB để chạy schema.sql
        // =========================================================================
        String schemaSql = readSchemaSQL();
        if (schemaSql == null || schemaSql.trim().isEmpty()) {
            logger.error("[DB-Setup] Bỏ qua thực thi schema do nội dung file trống hoặc không tìm thấy.");
            return;
        }

        List<String> sqlStatements = splitStatements(schemaSql);

        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl(this.dbUrl);
        dbConfig.setUsername(this.dbUser);
        dbConfig.setPassword(this.dbPassword);
        dbConfig.setMaximumPoolSize(3); // Giới hạn số lượng kết nối đồng thời khi khởi tạo cấu trúc bảng

        // Áp dụng các thuộc tính tối ưu hóa MySQL đồng bộ với cấu hình hệ thống
        dbConfig.addDataSourceProperty("cachePrepStmts", "true");
        dbConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dbConfig.addDataSourceProperty("useServerPrepStmts", "true");

        // Khởi tạo pool nạp cấu trúc, tự động giải phóng hoàn toàn sau khi chạy xong file SQL
        try (HikariDataSource dbDs = new HikariDataSource(dbConfig);
             Connection dbConn = dbDs.getConnection();
             Statement stmt = dbConn.createStatement()) {

            int successCount = 0;
            for (String sql : sqlStatements) {
                if (sql.trim().isEmpty()) continue;
                try {
                    stmt.execute(sql);
                    successCount++;
                } catch (SQLException e) {
                    if (isIgnorableSchemaError(e)) {
                        logger.info("[DB-Setup] Bỏ qua trạng thái bảng/cột đã tồn tại (Idempotent): {}", e.getMessage());
                    } else {
                        logger.error("[DB-Setup] Lỗi thực thi câu lệnh SQL: {}", sql);
                        logger.error("[DB-Setup] Chi tiết lỗi", e);
                    }
                }
            }
            logger.info("[DB-Setup] Hoàn thành chạy schema.sql. Thực thi thành công: {} câu lệnh.", successCount);

        } catch (SQLException e) {
            logger.error("[DB-Setup] Lỗi kết nối HikariCP tới database '{}' để chạy schema", dbName, e);
        }
    }

    /**
     * 2. Đọc file cấu hình database.properties để đồng bộ hóa thông tin kết nối.
     */
    private void loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                logger.warn("[DB-Setup] CẢNH BÁO: Không tìm thấy database.properties. Sử dụng cấu hình dự phòng.");
                this.dbUrl = "jdbc:mysql://localhost:3306/auction_db?useSSL=false&allowPublicKeyRetrieval=true";
                this.dbUser = "root";
                this.dbPassword = "Seductivemonke";
            } else {
                properties.load(input);
                this.dbUrl = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/auction_db");
                this.dbUser = properties.getProperty("db.username", "root");
                this.dbPassword = properties.getProperty("db.password", "Seductivemonke");
            }

            this.dbName = extractDatabaseName(this.dbUrl);

        } catch (IOException e) {
            logger.error("[DB-Setup] Lỗi nghiêm trọng khi đọc file cấu hình", e);
        }
    }

    /**
     * 3. Đọc nội dung file schema.sql từ thư mục resources (Đảm bảo mã hóa UTF-8).
     */
    private String readSchemaSQL() {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) {
                logger.error("[DB-Setup] Lỗi: Không tìm thấy file schema.sql trong thư mục resources.");
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("--") || line.trim().startsWith("#")) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            logger.error("[DB-Setup] Lỗi IO khi đọc file schema.sql", e);
        }
        return sb.toString();
    }

    /**
     * 4. Tách từng câu lệnh SQL dựa trên dấu phân tách ';'.
     */
    private List<String> splitStatements(String schema) {
        List<String> statements = new ArrayList<>();
        String[] rawStatements = schema.split(";");
        for (String raw : rawStatements) {
            String cleanSql = raw.trim();
            if (!cleanSql.isEmpty()) {
                statements.add(cleanSql);
            }
        }
        return statements;
    }

    /**
     * 5. Tạo URL kết nối tới cấp server (loại bỏ tên DB cụ thể) để phục vụ tạo database ban đầu.
     */
    private String buildAdminUrl(String url) {
        if (url == null) return "";
        return url.replaceAll("/[^/?]+(\\?|$)", "/$1");
    }

    /**
     * Hàm bổ trợ tách tên Database từ chuỗi JDBC URL tự động để tránh hardcode.
     */
    private String extractDatabaseName(String url) {
        try {
            Pattern pattern = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            logger.error("[DB-Setup] Không thể phân tách tên database từ URL: {}. Sử dụng tên mặc định.", url, e);
        }
        return "auction_db";
    }

    /**
     * 6. Bỏ qua một số mã lỗi phổ biến mang tính idempotent khi chạy lại cấu trúc schema cũ.
     */
    private boolean isIgnorableSchemaError(SQLException e) {
        String sqlState = e.getSQLState();
        int vendorCode = e.getErrorCode();

        // 1050 / 42S01: Table already exists
        // 1060 / 42S21: Duplicate column name
        return "42S01".equals(sqlState) || vendorCode == 1050 || "42S21".equals(sqlState) || vendorCode == 1060;
    }
}