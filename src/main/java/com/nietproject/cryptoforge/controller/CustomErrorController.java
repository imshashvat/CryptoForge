package com.nietproject.cryptoforge.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * Replaces Spring Boot's default Whitelabel Error Page with a premium
 * CryptoForge-branded error page. Implements ErrorController to intercept
 * all /error requests regardless of HTTP method or content type.
 *
 * This demonstrates Spring Boot's auto-configuration override pattern —
 * by implementing ErrorController, we take over the error handling endpoint.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles all error requests forwarded by Spring Boot's BasicErrorController.
     * Extracts the HTTP status code and exception message from request attributes.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpSession session, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exceptionAttr = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int status = 500;
        if (statusCode != null) {
            try { status = Integer.parseInt(statusCode.toString()); } catch (NumberFormatException ignored) {}
        }

        String title;
        String description;
        switch (status) {
            case 404 -> { title = "Page Not Found";        description = "The page you are looking for doesn't exist or has been moved."; }
            case 403 -> { title = "Access Denied";         description = "You don't have permission to access this resource."; }
            case 401 -> { title = "Authentication Required"; description = "Please log in to access this page."; }
            default  -> { title = "Something Went Wrong";  description = "An unexpected error occurred. Please try again."; }
        }

        // For 401/403 errors, redirect to login
        if (status == 401 || status == 403) {
            return "redirect:/login";
        }

        // Get exception detail (for dev — won't leak in prod since we only show title/desc)
        String exceptionDetail = "";
        if (exceptionAttr instanceof Throwable t) {
            exceptionDetail = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        } else if (errorMessage != null && !errorMessage.toString().isEmpty()) {
            exceptionDetail = errorMessage.toString();
        }

        model.addAttribute("status", status);
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("exceptionDetail", exceptionDetail);
        model.addAttribute("loggedIn", session != null && session.getAttribute("userId") != null);

        return "error";
    }
}
