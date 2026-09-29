package com.nietproject.cryptoforge.servlet;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * =====================================================================
 * =====================================================================
 *
 * Implements HttpSessionListener — directly named in the specification under
 * "Event and Listener" for Servlet API.
 *
 * Demonstrates:
 * - @WebListener                           → Listener registration
 * - implements HttpSessionListener         → Event & Listener pattern
 * - sessionCreated(HttpSessionEvent)       → Session lifecycle event
 * - sessionDestroyed(HttpSessionEvent)     → Session lifecycle event
 * - AtomicInteger                          → Thread-safe active session count
 *
 * The active session count is accessible via /api/admin/sessions endpoint.
 */
@WebListener
public class SessionTrackingListener implements HttpSessionListener {

    // Thread-safe counter — sessions can be created/destroyed concurrently
    private static final AtomicInteger activeSessions = new AtomicInteger(0);

    /**
     * Called by the servlet container when a new HttpSession is created.
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        int count = activeSessions.incrementAndGet();

        // Set a maximum session timeout (30 minutes)
        se.getSession().setMaxInactiveInterval(30 * 60);

        System.out.printf("[SessionTrackingListener] Session CREATED: %s | Active sessions: %d%n",
                se.getSession().getId(), count);
    }

    /**
     * Called by the servlet container when an HttpSession is invalidated,
     * expired, or the application shuts down.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        int count = activeSessions.decrementAndGet();

        System.out.printf("[SessionTrackingListener] Session DESTROYED: %s | Active sessions: %d%n",
                se.getSession().getId(), count);
    }

    /**
     * Static accessor — used by admin controller to expose active session count.
     */
    public static int getActiveSessionCount() {
        return activeSessions.get();
    }
}
