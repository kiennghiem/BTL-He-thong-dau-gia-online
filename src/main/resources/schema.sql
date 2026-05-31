USE auction_db;
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
    itemType VARCHAR(20),
    itemName VARCHAR(100) NOT NULL,
    description TEXT NULL,
    startingPrice DECIMAL(15, 2) DEFAULT 0.0,
    currentPrice DECIMAL(15, 2) DEFAULT 0.0,
    specialAttribute VARCHAR(45),
    ownerId VARCHAR(50),
    buyerId VARCHAR(50) NULL,
    FOREIGN KEY (ownerId) REFERENCES users(id),
    FOREIGN KEY (buyerId) REFERENCES users(id)
);

-- 3. BẢNG AUCTIONS (Tạo cuối cùng)
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20),
    title VARCHAR(100),
    description TEXT,
    startingPrice DECIMAL(15, 2),
    currentPrice DECIMAL(15, 2),
    startTime TIMESTAMP NULL DEFAULT NULL,
    endTime TIMESTAMP NULL DEFAULT NULL,
    itemId VARCHAR(50),
    highestBidderId VARCHAR(50) NULL,
    FOREIGN KEY (itemId) REFERENCES items(id),
    FOREIGN KEY (highestBidderId) REFERENCES users(id)
);

-- 4. BẢNG BIDS (Lịch sử đặt giá)
CREATE TABLE IF NOT EXISTS bids (
    id VARCHAR(50) PRIMARY KEY,
    auctionId VARCHAR(50),
    bidderId VARCHAR(50),
    bidAmount DECIMAL(15, 2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auctionId) REFERENCES auctions(id),
    FOREIGN KEY (bidderId) REFERENCES users(id)
);