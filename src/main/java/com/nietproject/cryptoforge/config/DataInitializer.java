package com.nietproject.cryptoforge.config;

import com.nietproject.cryptoforge.model.Asset;
import com.nietproject.cryptoforge.model.User;
import com.nietproject.cryptoforge.model.Wallet;
import com.nietproject.cryptoforge.repository.AssetRepository;
import com.nietproject.cryptoforge.repository.UserRepository;
import com.nietproject.cryptoforge.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DataInitializer — Seeds assets, admin & demo accounts, and creates stored procedure on first boot.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final List<String> ASSET_CODES = List.of(
        "BTC", "ETH", "SOL", "BNB", "DOGE", "XRP", "ADA", "AVAX", "MATIC", "LINK"
    );

    // Seed data: code → [name, price, 24h_change, market_cap]
    private static final Map<String, Object[]> ASSET_SEED = new LinkedHashMap<>();
    static {
        ASSET_SEED.put("BTC",   new Object[]{"Bitcoin",          "67540.00",  "2.34",  "1328000000000.00"});
        ASSET_SEED.put("ETH",   new Object[]{"Ethereum",          "3521.00",  "1.87",   "422000000000.00"});
        ASSET_SEED.put("SOL",   new Object[]{"Solana",             "178.50",  "3.12",    "82000000000.00"});
        ASSET_SEED.put("BNB",   new Object[]{"BNB",                "608.00",  "0.91",    "90000000000.00"});
        ASSET_SEED.put("XRP",   new Object[]{"XRP",                  "0.62",  "1.45",    "34000000000.00"});
        ASSET_SEED.put("ADA",   new Object[]{"Cardano",              "0.48", "-0.72",    "17000000000.00"});
        ASSET_SEED.put("AVAX",  new Object[]{"Avalanche",           "38.50",  "2.81",    "16000000000.00"});
        ASSET_SEED.put("MATIC", new Object[]{"Polygon",              "0.92",  "1.23",     "9000000000.00"});
        ASSET_SEED.put("LINK",  new Object[]{"Chainlink",           "18.20",  "3.54",    "11000000000.00"});
        ASSET_SEED.put("DOGE",  new Object[]{"Dogecoin",             "0.168", "-0.43",   "24000000000.00"});
    }

    private final UserRepository    userRepository;
    private final WalletRepository  walletRepository;
    private final AssetRepository   assetRepository;
    private final PasswordEncoder   passwordEncoder;
    private final DataSource        dataSource;

    public DataInitializer(UserRepository userRepository,
                           WalletRepository walletRepository,
                           AssetRepository assetRepository,
                           PasswordEncoder passwordEncoder,
                           DataSource dataSource) {
        this.userRepository  = userRepository;
        this.walletRepository = walletRepository;
        this.assetRepository  = assetRepository;
        this.passwordEncoder  = passwordEncoder;
        this.dataSource       = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAssets();
        seedAdmin();
        seedSuperAdmin();
        seedDemoUser();
        createStoredProcedure();
    }

    /**
     * Seed the assets table if empty.
     * Required for MarketDataPoller (.
     * DECIMAL(20,8) prices — never float (viva answer!).
     */
    private void seedAssets() {
        ASSET_SEED.forEach((code, data) -> {
            if (!assetRepository.existsById(code)) {
                Asset asset = new Asset();
                asset.setCode(code);
                asset.setName((String) data[0]);
                asset.setCurrentPriceUsd(new BigDecimal((String) data[1]));
                asset.setPriceChange24h(new BigDecimal((String) data[2]));
                asset.setMarketCapUsd(new BigDecimal((String) data[3]));
                assetRepository.save(asset);
                log.info("[DataInitializer] Seeded asset: {} ({})", data[0], code);
            }
        });
    }


    /**
     * Create the stored procedure sp_get_portfolio_value via raw JDBC.
     * Called once on every startup — DROP IF EXISTS makes it idempotent.
     */
    private void createStoredProcedure() {
        try (Connection conn = dataSource.getConnection();
             Statement  stmt = conn.createStatement()) {
            String dbName = conn.getMetaData().getDatabaseProductName().toLowerCase();
            if (dbName.contains("h2")) {
                stmt.execute("CREATE ALIAS IF NOT EXISTS sp_get_portfolio_value FOR \"com.nietproject.cryptoforge.config.DataInitializer.h2PortfolioValue\"");
                log.info("[DataInitializer] ✅ Stored procedure alias sp_get_portfolio_value created for H2 (.");
            } else {
                stmt.execute("DROP PROCEDURE IF EXISTS sp_get_portfolio_value");
                stmt.execute(
                    "CREATE PROCEDURE sp_get_portfolio_value(IN p_user_id BIGINT, OUT p_total_value DECIMAL(20,8)) " +
                    "BEGIN " +
                    "  SELECT IFNULL(SUM(w.balance * a.current_price_usd), 0) " +
                    "  INTO   p_total_value " +
                    "  FROM   wallets w " +
                    "  JOIN   assets  a ON w.currency_code = a.code " +
                    "  WHERE  w.user_id = p_user_id " +
                    "    AND  w.currency_code <> 'USD'; " +
                    "END"
                );
                log.info("[DataInitializer] ✅ Stored procedure sp_get_portfolio_value created for MySQL (.");
            }
        } catch (SQLException e) {
            log.warn("[DataInitializer] Could not create stored procedure — portfolio proc call will return 0: {}", e.getMessage());
        }
    }

    /**
     * H2 Stored Procedure Alias Callback
     */
    public static BigDecimal h2PortfolioValue(Connection conn, long userId) throws SQLException {
        String sql = "SELECT IFNULL(SUM(w.balance * a.current_price_usd), 0) FROM wallets w JOIN assets a ON w.currency_code = a.code WHERE w.user_id = ? AND w.currency_code <> 'USD'";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    private void seedAdmin() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin != null) {
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            log.info("[DataInitializer] Updated admin password to Admin@123");
            return;
        }
        admin = User.builder()
                .username("admin")
                .email("admin@cryptoforge.dev")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .active(true)
                .build();
        admin = userRepository.save(admin);

        // Admin gets $999,999 USD and crypto wallets
        walletRepository.save(Wallet.builder().user(admin).currencyCode("USD").balance(new BigDecimal("999999.00")).build());
        final User savedAdmin = admin;
        ASSET_CODES.forEach(code ->
            walletRepository.save(Wallet.builder().user(savedAdmin).currencyCode(code).balance(BigDecimal.ZERO).build())
        );
        log.info("[DataInitializer] ✅ Admin account created — username: admin / password: Admin@123");
    }

    private void seedSuperAdmin() {
        User superAdmin = userRepository.findByUsername("superadmin").orElse(null);
        if (superAdmin != null) {
            superAdmin.setPasswordHash(passwordEncoder.encode("SuperAdmin@123"));
            superAdmin.setRole(User.Role.ADMIN);
            userRepository.save(superAdmin);
            return;
        }
        superAdmin = User.builder()
                .username("superadmin")
                .email("owner@cryptoforge.dev")
                .passwordHash(passwordEncoder.encode("SuperAdmin@123"))
                .role(User.Role.ADMIN)
                .active(true)
                .build();
        superAdmin = userRepository.save(superAdmin);

        walletRepository.save(Wallet.builder().user(superAdmin).currencyCode("USD").balance(new BigDecimal("999999.00")).build());
        final User savedSuperAdmin = superAdmin;
        ASSET_CODES.forEach(code ->
            walletRepository.save(Wallet.builder().user(savedSuperAdmin).currencyCode(code).balance(BigDecimal.ZERO).build())
        );
        log.info("[DataInitializer] ✅ Second Admin created — username: superadmin / password: SuperAdmin@123");
    }

    private void seedDemoUser() {
        User demo = userRepository.findByUsername("demo_user").orElse(null);
        if (demo != null) {
            demo.setPasswordHash(passwordEncoder.encode("Demo@1234"));
            userRepository.save(demo);
            log.info("[DataInitializer] Updated demo_user password to Demo@1234");
            return;
        }
        demo = User.builder()
                .username("demo_user")
                .email("demo@cryptoforge.dev")
                .passwordHash(passwordEncoder.encode("Demo@1234"))
                .role(User.Role.USER)
                .active(true)
                .build();
        demo = userRepository.save(demo);

        walletRepository.save(Wallet.builder().user(demo).currencyCode("USD").balance(new BigDecimal("10000.00")).build());
        final User savedDemo = demo;
        ASSET_CODES.forEach(code ->
            walletRepository.save(Wallet.builder().user(savedDemo).currencyCode(code).balance(BigDecimal.ZERO).build())
        );
        log.info("[DataInitializer] ✅ Demo user created — username: demo_user / password: Demo@1234");
    }
}
