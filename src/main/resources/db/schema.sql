-- ============================================================
-- CryptoForge — MySQL Schema
-- Run: mysql -u root -p cryptoforge < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS cryptoforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cryptoforge;

-- -------- USERS --------
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role         ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------- WALLETS --------
-- DECIMAL(20,8): never FLOAT/DOUBLE for financial values (viva answer!)
CREATE TABLE IF NOT EXISTS wallets (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT         NOT NULL,
    currency_code VARCHAR(10)    NOT NULL,
    balance       DECIMAL(20,8)  NOT NULL DEFAULT 0.00000000,
    version       BIGINT         NOT NULL DEFAULT 0,
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user   FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_wallet        UNIQUE (user_id, currency_code)
) ENGINE=InnoDB;

-- -------- ASSETS (crypto prices) --------
CREATE TABLE IF NOT EXISTS assets (
    code              VARCHAR(10)   PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL,
    current_price_usd DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    price_change_24h  DECIMAL(10,4) NOT NULL DEFAULT 0.0000,
    market_cap_usd    DECIMAL(30,2) NOT NULL DEFAULT 0.00,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------- ORDERS --------
CREATE TABLE IF NOT EXISTS orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    asset_code     VARCHAR(10)   NOT NULL,
    order_type     ENUM('BUY','SELL') NOT NULL,
    quantity       DECIMAL(20,8) NOT NULL,
    price_at_order DECIMAL(20,8) NOT NULL,
    total_value    DECIMAL(20,8) NOT NULL,
    status         ENUM('PENDING','COMPLETED','FAILED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_order_asset   FOREIGN KEY (asset_code) REFERENCES assets(code)
) ENGINE=InnoDB;

-- Index for fast user order history queries
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- -------- TRANSACTIONS (ledger) --------
CREATE TABLE IF NOT EXISTS transactions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    user_id         BIGINT        NOT NULL,
    type            ENUM('BUY','SELL') NOT NULL,
    asset_code      VARCHAR(10)   NOT NULL,
    quantity        DECIMAL(20,8) NOT NULL,
    price_usd       DECIMAL(20,8) NOT NULL,
    total_value_usd DECIMAL(20,8) NOT NULL,
    fee_usd         DECIMAL(20,8) NOT NULL DEFAULT 0.00000000,
    balance_after   DECIMAL(20,8) NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_tx_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    CONSTRAINT fk_tx_asset FOREIGN KEY (asset_code) REFERENCES assets(code)
) ENGINE=InnoDB;

CREATE INDEX idx_tx_user_id ON transactions(user_id);

-- -------- LOGIN AUDIT (Unit I — raw Servlet + JDBC demo) --------
CREATE TABLE IF NOT EXISTS login_audit (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL,
    ip_address  VARCHAR(45) NOT NULL,
    user_agent  VARCHAR(255),
    success     BOOLEAN NOT NULL DEFAULT TRUE,
    login_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- STORED PROCEDURE (Unit I — CallableStatement demo)
-- Returns total portfolio value in USD for a given user
-- ============================================================
DELIMITER $$

DROP PROCEDURE IF EXISTS sp_get_portfolio_value$$

CREATE PROCEDURE sp_get_portfolio_value(IN p_user_id BIGINT, OUT p_total_value DECIMAL(20,8))
BEGIN
    SELECT IFNULL(SUM(w.balance * a.current_price_usd), 0)
    INTO   p_total_value
    FROM   wallets w
    JOIN   assets  a ON w.currency_code = a.code
    WHERE  w.user_id = p_user_id
      AND  w.currency_code <> 'USD';
END$$

DELIMITER ;
