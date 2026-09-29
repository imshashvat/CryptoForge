package com.nietproject.cryptoforge.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

/**
 * =====================================================================
 * =====================================================================
 *
 * This servlet is intentionally outside the Spring MVC DispatcherServlet
 * path to demonstrate raw Servlet API — a specific specification requirement.
 *
 * Demonstrates:
 * - @WebServlet annotation                → Servlet API
 * - extends HttpServlet                   → HTTP Servlet class
 * - doPost() override                     → HTTP method handling
 * - HttpServletRequest / Response         → Servlet Interface usage
 * - DriverManager.getConnection()         → JDBC Driver + Connection
 * - PreparedStatement                     → JDBC PreparedStatement
 * - executeUpdate()                       → DML execution
 * - try-with-resources (auto-close)       → Resource management
 *
 * In production: Spring Security guarantees this endpoint is only
 * reachable after authentication, but the servlet itself manages
 * its own JDBC connection (no JPA, no Spring context involvement).
 */
@WebServlet("/audit/login")
public class LoginAuditServlet extends HttpServlet {

    // JDBC connection details come from system environment variables — no hardcoding
    private static final String JDBC_URL_ENV  = "DATABASE_URL";
    private static final String DB_USER_ENV   = "DB_USERNAME";
    private static final String DB_PASS_ENV   = "DB_PASSWORD";

    // Fallback for local dev
    private static final String DEFAULT_URL   = "jdbc:mysql://localhost:3307/cryptoforge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER  = "root";
    private static final String DEFAULT_PASS  = "";

    /**
     * Servlet Lifecycle — init()
     * Called once when servlet is first loaded.
     * Registers the JDBC driver explicitly (.
     */
    @Override
    public void init() throws ServletException {
        try {
            // Explicit driver registration
            Class.forName("com.mysql.cj.jdbc.Driver");
            log("LoginAuditServlet initialized — MySQL JDBC driver registered.");
        } catch (ClassNotFoundException e) {
            throw new ServletException("MySQL JDBC driver not found", e);
        }
    }

    /**
     * Handles POST requests to /audit/login
     * Records a login attempt in the login_audit table.
     *
     * Called by AuthController after a successful/failed login
     * via a server-side HttpClient call (not exposed directly to users).
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username  = req.getParameter("username");
        String ipAddress = req.getRemoteAddr();
        String userAgent = req.getHeader("User-Agent");
        boolean success  = Boolean.parseBoolean(req.getParameter("success"));

        // Validate inputs
        if (username == null || username.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Raw JDBC
        String jdbcUrl = System.getenv(JDBC_URL_ENV) != null
                ? System.getenv(JDBC_URL_ENV) : DEFAULT_URL;
        String dbUser  = System.getenv(DB_USER_ENV) != null
                ? System.getenv(DB_USER_ENV) : DEFAULT_USER;
        String dbPass  = System.getenv(DB_PASS_ENV) != null
                ? System.getenv(DB_PASS_ENV) : DEFAULT_PASS;

        // try-with-resources: auto-closes Connection and PreparedStatement
        try (Connection conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO login_audit (username, ip_address, user_agent, success, login_time) " +
                     "VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, ipAddress);
            ps.setString(3, userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 255)) : "unknown");
            ps.setBoolean(4, success);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate(); 

            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            // Log but don't crash — audit failure should never break login
            log("LoginAuditServlet: Failed to write audit record — " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Servlet Lifecycle — destroy()
     * Called when servlet is unloaded (app shutdown).
     */
    @Override
    public void destroy() {
        log("LoginAuditServlet destroyed.");
    }
}
