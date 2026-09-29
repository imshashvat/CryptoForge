package com.nietproject.cryptoforge.servlet;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * =====================================================================
 * =====================================================================
 *
 * Demonstrates the full Servlet Filter lifecycle:
 * - init(FilterConfig)         — called once when filter is initialized
 * - doFilter(req, resp, chain) — called for every matching request
 * - destroy()                  — called once when filter is removed
 *
 * Protects all /order/* URLs at the raw Servlet layer.
 * This is in ADDITION to Spring Security — demonstrates both mechanisms
 * (a deliberate choice worth explaining in the viva).
 *
 * NOTE: @WebFilter is registered because @ServletComponentScan
 * is present on CryptoForgeApplication —.
 */
@WebFilter(urlPatterns = {"/order/*", "/portfolio/*"})
public class AuthFilter implements Filter {

    /**
     * Filter Lifecycle — init()
     * FilterConfig provides servlet context and init parameters.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String appName = filterConfig.getServletContext().getServletContextName();
        System.out.println("[AuthFilter] Initialized for application: " + appName);
    }

    /**
     * Filter Lifecycle — doFilter()
     * The core of the filter — checks for active session before allowing
     * the request to proceed down the filter chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq  = (HttpServletRequest)  request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        // getSession(false)
        // false = do NOT create a new session if one doesn't exist
        HttpSession session = httpReq.getSession(false);

        boolean isAuthenticated = (session != null && session.getAttribute("userId") != null);

        if (!isAuthenticated) {
            // Redirect to login — servlet-layer access control
            httpResp.sendRedirect(httpReq.getContextPath() + "/login");
            return; // MUST return after redirect — do not pass to chain
        }

        // Continue processing — pass to next filter or servlet
        chain.doFilter(request, response);
    }

    /**
     * Filter Lifecycle — destroy()
     * Called when the web application shuts down.
     */
    @Override
    public void destroy() {
        System.out.println("[AuthFilter] Destroyed — cleanup complete.");
    }
}
