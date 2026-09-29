<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
    <script>(function(){var t=localStorage.getItem('cf-theme')||'light';document.documentElement.setAttribute('data-theme',t);})();</script>
</head>
<body class="app-shell">

<nav class="site-nav">
    <div class="nav-inner">
        <a href="/" class="nav-logo">
            <div class="nav-logo-mark">⬡</div>
            CryptoForge
        </a>
    </div>
</nav>

<div class="page-wrap animate-in">
    <div style="max-width:560px;margin:var(--space-16) auto;text-align:center;">
        <div style="font-size:80px;margin-bottom:var(--space-6);">
            <%
                Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
                if (statusCode != null && statusCode == 404) {
                    out.print("🔍");
                } else {
                    out.print("⚠️");
                }
            %>
        </div>
        <h1 style="font-family:var(--font-display);font-size:36px;font-weight:800;color:var(--text-primary);letter-spacing:-0.02em;margin-bottom:var(--space-3);">
            <%
                if (statusCode != null && statusCode == 404) {
                    out.print("Page Not Found");
                } else {
                    out.print("Something went wrong");
                }
            %>
        </h1>
        <p style="font-size:16px;color:var(--text-secondary);line-height:1.7;margin-bottom:var(--space-8);">
            <%
                if (statusCode != null && statusCode == 404) {
                    out.print("The page you're looking for doesn't exist or has been moved.");
                } else {
                    String errMsg = (String) request.getAttribute("javax.servlet.error.message");
                    if (errMsg != null && !errMsg.isEmpty()) {
                        out.print(org.springframework.web.util.HtmlUtils.htmlEscape(errMsg));
                    } else {
                        out.print("An unexpected error occurred. Please try again or return to the dashboard.");
                    }
                }
            %>
        </p>

        <c:if test="${not empty param.status}">
            <div style="background:var(--bg-subtle);border:1px solid var(--border);border-radius:var(--radius-md);padding:var(--space-3) var(--space-5);margin-bottom:var(--space-6);display:inline-block;">
                <span style="font-family:var(--font-mono);font-size:13px;color:var(--text-muted);">HTTP ${param.status}</span>
            </div>
        </c:if>
        <%
            if (statusCode != null) {
        %>
        <div style="background:var(--bg-subtle);border:1px solid var(--border);border-radius:var(--radius-md);padding:var(--space-3) var(--space-5);margin-bottom:var(--space-6);display:inline-block;">
            <span style="font-family:var(--font-mono);font-size:13px;color:var(--text-muted);">HTTP <%= statusCode %></span>
        </div>
        <%
            }
        %>

        <div style="display:flex;gap:var(--space-3);justify-content:center;flex-wrap:wrap;">
            <a href="/dashboard" class="btn btn-primary btn-lg">Go to Dashboard</a>
            <a href="/"          class="btn btn-secondary btn-lg">Back to Home</a>
        </div>
    </div>
</div>

<script src="/assets/js/forge.js"></script>
</body>
</html>
