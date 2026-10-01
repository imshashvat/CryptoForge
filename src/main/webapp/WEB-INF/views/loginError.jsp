<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Failed — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
</head>
<body>
<div class="auth-shell">
    <div class="auth-brand">
        <div class="auth-brand-dots"></div>
        <div class="auth-brand-logo">
            <div class="auth-brand-logo-mark">⬡</div>
            CryptoForge
        </div>
        <p class="auth-brand-tagline">Secure, professional crypto paper trading platform.</p>
    </div>
    <div class="auth-form-wrap">
        <div class="auth-form-inner animate-in">
            <div class="flash flash-error" style="margin-bottom:var(--space-6);">
                ⚠ Invalid credentials. Please check your username and password.
            </div>
            <h1 class="auth-form-title">Sign In Failed</h1>
            <p class="auth-form-sub">Your username or password is incorrect.</p>
            <div style="display:flex;flex-direction:column;gap:var(--space-3);margin-top:var(--space-6);">
                <a href="/login"    class="btn btn-primary btn-full btn-lg">Try Again</a>
                <a href="/register" class="btn btn-secondary btn-full">Create Account</a>
            </div>
        </div>
    </div>
</div>
<script src="/assets/js/forge.js"></script>
</body>
</html>
