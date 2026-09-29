package com.nietproject.cryptoforge.service;

import com.nietproject.cryptoforge.dto.OrderRequest;
import com.nietproject.cryptoforge.dto.OrderResult;
import com.nietproject.cryptoforge.exception.InsufficientFundsException;
import com.nietproject.cryptoforge.exception.InsufficientHoldingsException;
import com.nietproject.cryptoforge.exception.OrderExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * =====================================================================
 * =====================================================================
 *
 * This is the most important service in the project.
 * Uses RAW JDBC deliberately — not JPA — to demonstrate.
 *
 * Demonstrates:
 * - DataSource / Connection              — JDBC Connection
 * - conn.setAutoCommit(false)            — Transaction Management: BEGIN
 * - PreparedStatement                    — JDBC PreparedStatement
 * - conn.commit()                        — Transaction Management: COMMIT
 * - conn.rollback()                      — Transaction Management: ROLLBACK
 * - SELECT ... FOR UPDATE                — Pessimistic locking (concurrency)
 * - CallableStatement                    — Stored Procedures 
 * - ResultSet                            — Reading query results
 * - ReentrantLock / ConcurrentHashMap    — Application-level concurrency (Unit XII)
 *
 * The DataSource is Spring-managed HikariCP — auto-configured by Spring Boot .
 * But the Connection is manually obtained and managed here — that's the.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final DataSource dataSource;

    // Application-level per-wallet locks (supplement to DB-level FOR UPDATE)
    // ConcurrentHashMap — Unit XII multithreading supplement
    private final ConcurrentHashMap<Long, ReentrantLock> walletLocks = new ConcurrentHashMap<>();

    /**
     * Constructor Injection — DataSource is auto-configured HikariCP pool (Spring Boot.
     */
    public OrderService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Place a BUY or SELL order.
     *
     * This entire method runs inside ONE database transaction.
     * Either ALL steps succeed and commit, or ANY failure rolls back EVERYTHING.
     * No partial state ever persists — financial integrity guaranteed.
     *
     * @param req OrderRequest DTO from the controller
     * @return OrderResult with trade details
     * @throws OrderExecutionException if anything fails (wraps original cause)
     */
    public OrderResult placeOrder(OrderRequest req) {
        long userId = req.getUserId();
        String assetCode = req.getAssetCode().toUpperCase();
        String orderType = req.getOrderType().toUpperCase();
        BigDecimal quantity = req.getQuantity();

        log.info("Order request: userId={} type={} asset={} qty={}", userId, orderType, assetCode, quantity);

        // Try (Connection) — try-with-resources auto-closes the connection
        try (Connection conn = dataSource.getConnection()) {

            // ---- BEGIN TRANSACTION ----
            conn.setAutoCommit(false);  

            try {
                // Step 1: Lock and read the current asset price
                // SELECT ... FOR UPDATE = pessimistic lock on the assets row
                // Prevents another thread from reading a stale price simultaneously
                BigDecimal price = getAssetPriceForUpdate(conn, assetCode);

                BigDecimal totalCost = price.multiply(quantity);

                // Step 2: Read wallet balances
                BigDecimal usdBalance   = getWalletBalance(conn, userId, "USD");
                BigDecimal assetBalance = getWalletBalance(conn, userId, assetCode);

                // Step 3: Validate and update balances
                if ("BUY".equals(orderType)) {
                    if (usdBalance.compareTo(totalCost) < 0) {
                        throw new InsufficientFundsException(
                            String.format("Insufficient USD balance. Required: $%.2f, Available: $%.2f",
                                totalCost, usdBalance));
                    }
                    updateWalletBalance(conn, userId, "USD",     usdBalance.subtract(totalCost));
                    updateWalletBalance(conn, userId, assetCode, assetBalance.add(quantity));

                } else { // SELL
                    if (assetBalance.compareTo(quantity) < 0) {
                        throw new InsufficientHoldingsException(
                            String.format("Insufficient %s. Required: %s, Available: %s",
                                assetCode, quantity, assetBalance));
                    }
                    updateWalletBalance(conn, userId, assetCode, assetBalance.subtract(quantity));
                    updateWalletBalance(conn, userId, "USD",     usdBalance.add(totalCost));
                }

                // Step 4: Insert order record
                long orderId = insertOrder(conn, userId, assetCode, orderType, quantity, price, totalCost);

                // Step 5: Insert ledger (transaction) entry
                BigDecimal newUsdBalance = getWalletBalance(conn, userId, "USD");
                insertLedgerEntry(conn, orderId, userId, orderType, assetCode, quantity, price, totalCost, newUsdBalance);

                // ---- COMMIT ---- all-or-nothing
                conn.commit();

                log.info("Order {} committed: orderId={} type={} asset={} qty={} price={} total={}",
                    "SUCCESS", orderId, orderType, assetCode, quantity, price, totalCost);

                return OrderResult.builder()
                        .orderId(orderId)
                        .status("SUCCESS")
                        .message("Order executed successfully")
                        .assetCode(assetCode)
                        .orderType(orderType)
                        .quantity(quantity)
                        .priceAtExecution(price)
                        .totalValue(totalCost)
                        .newUsdBalance(newUsdBalance)
                        .newAssetBalance(getWalletBalance(conn, userId, assetCode))
                        .executedAt(LocalDateTime.now())
                        .build();

            } catch (InsufficientFundsException | InsufficientHoldingsException e) {
                conn.rollback(); // ---- ROLLBACK ---- Unit I
                throw e; // re-throw — handled by GlobalExceptionHandler

            } catch (Exception e) {
                conn.rollback(); // ---- ROLLBACK ---- Unit I
                throw new OrderExecutionException("Unexpected error during order execution", e);
            }

        } catch (SQLException e) {
            throw new OrderExecutionException("Database connection failed", e);
        }
    }

    /**
     * Calls the stored procedure sp_get_portfolio_value.
     *
     * @param userId the user whose portfolio value to compute
     * @return total crypto portfolio value in USD
     */
    public BigDecimal getPortfolioValueViaProcedure(long userId) {
        try (Connection conn = dataSource.getConnection()) {
            boolean isH2 = conn.getMetaData().getDatabaseProductName().toLowerCase().contains("h2");
            String callSql = isH2 ? "{?= call sp_get_portfolio_value(?)}" : "{call sp_get_portfolio_value(?, ?)}";
            try (CallableStatement cs = conn.prepareCall(callSql)) {
                if (isH2) {
                    cs.registerOutParameter(1, Types.DECIMAL);
                    cs.setLong(2, userId);
                    cs.execute();
                    BigDecimal value = cs.getBigDecimal(1);
                    return value != null ? value : BigDecimal.ZERO;
                } else {
                    cs.setLong(1, userId);
                    cs.registerOutParameter(2, Types.DECIMAL);
                    cs.execute();
                    BigDecimal value = cs.getBigDecimal(2);
                    return value != null ? value : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            log.error("Stored procedure call failed for userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    // ================================================================
    // Private JDBC helpers — all use PreparedStatement 
    // ================================================================

    /**
     * SELECT ... FOR UPDATE — reads asset price AND locks the row.
     * Other transactions trying to read/update this row must wait.
     */
    private BigDecimal getAssetPriceForUpdate(Connection conn, String assetCode) throws SQLException {
        String sql = "SELECT current_price_usd FROM assets WHERE code = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assetCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Asset not found: " + assetCode);
                return rs.getBigDecimal("current_price_usd");
            }
        }
    }

    private BigDecimal getWalletBalance(Connection conn, long userId, String currencyCode) throws SQLException {
        String sql = "SELECT balance FROM wallets WHERE user_id = ? AND currency_code = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, currencyCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Wallet not found: " + currencyCode + " for userId=" + userId);
                return rs.getBigDecimal("balance");
            }
        }
    }

    private void updateWalletBalance(Connection conn, long userId, String currencyCode, BigDecimal newBalance)
            throws SQLException {
        String sql = "UPDATE wallets SET balance = ? WHERE user_id = ? AND currency_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setLong(2, userId);
            ps.setString(3, currencyCode);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Wallet update affected 0 rows — wallet missing");
        }
    }

    private long insertOrder(Connection conn, long userId, String assetCode, String orderType,
                             BigDecimal quantity, BigDecimal price, BigDecimal totalValue) throws SQLException {
        String sql = "INSERT INTO orders (user_id, asset_code, order_type, quantity, price_at_order, total_value, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'COMPLETED', ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setString(2, assetCode);
            ps.setString(3, orderType);
            ps.setBigDecimal(4, quantity);
            ps.setBigDecimal(5, price);
            ps.setBigDecimal(6, totalValue);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Order insert returned no generated key");
                return keys.getLong(1);
            }
        }
    }

    private void insertLedgerEntry(Connection conn, long orderId, long userId, String type,
                                   String assetCode, BigDecimal quantity, BigDecimal price,
                                   BigDecimal totalValue, BigDecimal balanceAfter) throws SQLException {
        String sql = "INSERT INTO transactions (order_id, user_id, type, asset_code, quantity, price_usd, " +
                     "total_value_usd, fee_usd, balance_after, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, userId);
            ps.setString(3, type);
            ps.setString(4, assetCode);
            ps.setBigDecimal(5, quantity);
            ps.setBigDecimal(6, price);
            ps.setBigDecimal(7, totalValue);
            ps.setBigDecimal(8, BigDecimal.ZERO);
            ps.setBigDecimal(9, balanceAfter);
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }
}
