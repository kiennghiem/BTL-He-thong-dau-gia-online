-- 1. BẢNG USERS (Phải tạo đầu tiên và PHẢI CÓ CỘT id)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY, -- Bắt buộc phải có dòng này để tự sinh chuỗi ID
    role VARCHAR(30) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.0
    );

-- 2. BẢNG ITEMS (Tạo thứ hai)
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    item_type VARCHAR(20),
    item_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    starting_price DECIMAL(15, 2) 0.0,
    current_price DECIMAL(15, 2) 0.0,
    special_attribute VARCHAR(45),
    owner_id VARCHAR(50),
    buyer_id VARCHAR(50) NULL,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- 3. BẢNG AUCTIONS (Tạo cuối cùng)
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20),
    title VARCHAR(100),
    description TEXT,
    starting_price DECIMAL(15, 2),
    current_price DECIMAL(15, 2),
    start_time TIMESTAMP NULL DEFAULT NULL,
    end_time TIMESTAMP NULL DEFAULT NULL,
    item_id VARCHAR(50),
    highest_bidder_id VARCHAR(50) NULL,
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
);