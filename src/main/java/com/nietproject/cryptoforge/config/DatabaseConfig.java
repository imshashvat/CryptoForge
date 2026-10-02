package com.nietproject.cryptoforge.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Production Database Configuration for Render & Cloud MySQL (Aiven / Railway).
 *
 * Automatically detects whether DATABASE_URL is:
 * 1. Standard JDBC format: jdbc:mysql://host:port/db?ssl-mode=REQUIRED
 * 2. Cloud URI format:    mysql://user:pass@host:port/db?ssl-mode=REQUIRED
 *
 * If given a Cloud URI (like Aiven's Service URI), it extracts credentials
 * and normalizes the URL to a compliant JDBC string so driver errors never occur.
 */
@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DATABASE_URL:${spring.datasource.url:}}")
    private String databaseUrl;

    @Value("${DB_USERNAME:${spring.datasource.username:}}")
    private String dbUsername;

    @Value("${DB_PASSWORD:${spring.datasource.password:}}")
    private String dbPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String cleanUrl = databaseUrl;
        String user = dbUsername;
        String pass = dbPassword;

        if (cleanUrl != null && (cleanUrl.startsWith("mysql://") || cleanUrl.startsWith("postgres://"))) {
            try {
                URI uri = new URI(cleanUrl);
                String userInfo = uri.getUserInfo();
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    if (user == null || user.isBlank()) {
                        user = parts[0];
                    }
                    if (pass == null || pass.isBlank()) {
                        pass = parts[1];
                    }
                }
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 3306;
                String path = uri.getPath();
                String query = uri.getQuery();
                cleanUrl = "jdbc:mysql://" + host + ":" + port + path + (query != null && !query.isBlank() ? "?" + query : "");
                log.info("Normalized Cloud URI to JDBC format: jdbc:mysql://{}:{}{}", host, port, path);
            } catch (Exception e) {
                log.warn("Could not parse URI format of DATABASE_URL, attempting prefix fallback: {}", e.getMessage());
                if (!cleanUrl.startsWith("jdbc:")) {
                    cleanUrl = "jdbc:" + cleanUrl;
                }
            }
        } else if (cleanUrl != null && !cleanUrl.startsWith("jdbc:") && cleanUrl.contains("://")) {
            cleanUrl = "jdbc:" + cleanUrl;
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(cleanUrl);
        if (user != null && !user.isBlank()) {
            config.setUsername(user);
        }
        if (pass != null && !pass.isBlank()) {
            config.setPassword(pass);
        }
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setKeepaliveTime(30000);
        config.setValidationTimeout(10000);
        config.setLeakDetectionThreshold(60000);
        config.addDataSourceProperty("connectTimeout", "15000");
        config.addDataSourceProperty("socketTimeout", "30000");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }
}
