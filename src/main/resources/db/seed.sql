-- ============================================================
-- CryptoForge — Seed Data
-- ============================================================
USE cryptoforge;

-- -------- Seed Assets (5 cryptocurrencies) --------
INSERT INTO assets (code, name, current_price_usd, price_change_24h, market_cap_usd) VALUES
('BTC',  'Bitcoin',  67540.00000000,  2.34,  1328000000000.00),
('ETH',  'Ethereum',  3521.00000000,  1.87,   422000000000.00),
('SOL',  'Solana',     178.50000000,  3.12,    82000000000.00),
('BNB',  'BNB',        608.00000000,  0.91,    90000000000.00),
('DOGE', 'Dogecoin',     0.16800000, -0.43,    24000000000.00)
ON DUPLICATE KEY UPDATE
    current_price_usd = VALUES(current_price_usd),
    price_change_24h  = VALUES(price_change_24h),
    market_cap_usd    = VALUES(market_cap_usd);

-- -------- Admin User (password: Admin@123 — BCrypt hash) --------
-- To regenerate hash: new BCryptPasswordEncoder().encode("Admin@123")
INSERT IGNORE INTO users (username, email, password_hash, role)
VALUES ('admin', 'admin@cryptoforge.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN');

-- Admin wallet seeded with $10,000 USD and some crypto
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'USD',  10000.00000000 FROM users WHERE username = 'admin';
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'BTC',  0.15000000     FROM users WHERE username = 'admin';
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'ETH',  2.50000000     FROM users WHERE username = 'admin';
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'SOL',  10.00000000    FROM users WHERE username = 'admin';
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'BNB',  1.00000000     FROM users WHERE username = 'admin';
INSERT IGNORE INTO wallets (user_id, currency_code, balance)
SELECT id, 'DOGE', 1000.00000000  FROM users WHERE username = 'admin';
