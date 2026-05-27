-- Online Auction System Schema

-- Users Table: Stores credentials and roles
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    balance DOUBLE DEFAULT 0.0
);

-- Items Table: Stores polymorphic items (Electronic, Art, Vehicle)
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    itemName VARCHAR(100) NOT NULL,
    description TEXT,
    currentPrice DOUBLE DEFAULT 0.0,
    startingPrice DOUBLE NOT NULL,
    startingTime TIMESTAMP,
    closingTime TIMESTAMP,
    status VARCHAR(20),
    owner_id VARCHAR(50),
    current_bidder_id VARCHAR(50),
    buyer_id VARCHAR(50),
    item_type VARCHAR(20),
    brand VARCHAR(50),        -- For Electronics/Vehicle
    artist_name VARCHAR(50),  -- For Art
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (current_bidder_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- Auctions Table: Redundant but used by AuctionDAO
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(50) PRIMARY KEY,
    item_id VARCHAR(50),
    title VARCHAR(100),
    description TEXT,
    starting_price DECIMAL(15, 2),
    current_price DECIMAL(15, 2),
    highest_bidder_id VARCHAR(50),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20),
    FOREIGN KEY (item_id) REFERENCES items(id),
    FOREIGN KEY (highest_bidder_id) REFERENCES users(id)
);

-- Bids Table: Transaction history
CREATE TABLE IF NOT EXISTS bids (
    id VARCHAR(50) PRIMARY KEY,
    itemId VARCHAR(50),
    bidderId VARCHAR(50),
    bidAmount DECIMAL(15, 2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (itemId) REFERENCES items(id),
    FOREIGN KEY (bidderId) REFERENCES users(id)
);
