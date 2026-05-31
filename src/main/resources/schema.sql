-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE auction_db;

-- 2. TABLE: users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    role VARCHAR(30) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.0
);

-- 3. TABLE: items
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    itemType VARCHAR(50) NOT NULL,
    itemName VARCHAR(255) NOT NULL,
    description TEXT,
    startingPrice DECIMAL(15, 2) DEFAULT 0.0,
    currentPrice DECIMAL(15, 2) DEFAULT 0.0,
    specialAttribute VARCHAR(255),
    ownerId VARCHAR(50) NOT NULL,
    buyerId VARCHAR(50) NULL,
    FOREIGN KEY (ownerId) REFERENCES users(id),
    FOREIGN KEY (buyerId) REFERENCES users(id)
);

-- 4. TABLE: auctions
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    startingPrice DECIMAL(15, 2) DEFAULT 0.0,
    currentPrice DECIMAL(15, 2) DEFAULT 0.0,
    startTime TIMESTAMP NULL DEFAULT NULL,
    endTime TIMESTAMP NULL DEFAULT NULL,
    itemId VARCHAR(50) NOT NULL,
    highestBidderId VARCHAR(50) NULL,
    FOREIGN KEY (itemId) REFERENCES items(id),
    FOREIGN KEY (highestBidderId) REFERENCES users(id)
);

-- 5. TABLE: bids (Bid history)
CREATE TABLE IF NOT EXISTS bids (
    id VARCHAR(50) PRIMARY KEY,
    auctionId VARCHAR(50) NOT NULL,
    bidderId VARCHAR(50) NOT NULL,
    bidAmount DECIMAL(15, 2) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auctionId) REFERENCES auctions(id),
    FOREIGN KEY (bidderId) REFERENCES users(id)
);
