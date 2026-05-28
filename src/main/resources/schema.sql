USE auction_db;
-- 1. BẢNG USERS (Phải tạo đầu tiên và PHẢI CÓ CỘT id)
CREATE TABLE IF NOT EXISTS users (
                                     id VARCHAR(50) PRIMARY KEY DEFAULT (UUID()), -- Bắt buộc phải có dòng này để tự sinh chuỗi ID
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    balance DOUBLE DEFAULT 0.0
    );

-- 2. BẢNG ITEMS (Tạo thứ hai)
CREATE TABLE IF NOT EXISTS items (
                                     id VARCHAR(50) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    owner_id VARCHAR(50),
    FOREIGN KEY (owner_id) REFERENCES users(id)
    );

-- 3. BẢNG AUCTIONS (Tạo cuối cùng)
CREATE TABLE IF NOT EXISTS auctions (
                                        id VARCHAR(50) PRIMARY KEY DEFAULT (UUID()),
    item_id VARCHAR(50),
    title VARCHAR(100),
    description TEXT,
    starting_price DECIMAL(15, 2),
    current_price DECIMAL(15, 2),
    highest_bidder_id VARCHAR(50),
    start_time TIMESTAMP NULL DEFAULT NULL,
    end_time TIMESTAMP NULL DEFAULT NULL,
    status VARCHAR(20),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
    );