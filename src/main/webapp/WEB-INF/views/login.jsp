<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In — CryptoForge</title>
    <meta name="description" content="Sign in to your CryptoForge account and start trading cryptocurrencies.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:opsz,wght@14..32,300;14..32,400;14..32,500;14..32,600;14..32,700;14..32,800&family=Manrope:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
      *, *::before, *::after { box-sizing:border-box; margin:0; padding:0; }
      html, body { height:100%; }
      body {
        font-family:'Inter',system-ui,sans-serif;
        background:#0B0E11;
        color:#FFFFFF;
        display:flex;
        min-height:100vh;
        -webkit-font-smoothing:antialiased;
      }

      /* ── Shell ── */
      .auth-shell {
        display:grid;
        grid-template-columns:1fr 1fr;
        width:100%;
        min-height:100vh;
      }

      /* ── LEFT: Brand panel ── */
      .auth-brand {
        position:relative;
        overflow:hidden;
        display:flex;
        flex-direction:column;
        align-items:center;
        justify-content:center;
        padding:56px 48px;
        background:#0B0E11;
      }
      /* Green radial glow — top right */
      .auth-brand::before {
        content:'';
        position:absolute;
        top:-200px; right:-100px;
        width:700px; height:700px;
        background:radial-gradient(circle, rgba(0,214,143,0.18) 0%, transparent 60%);
        pointer-events:none;
      }
      /* Subtle grid pattern */
      .auth-brand::after {
        content:'';
        position:absolute; inset:0;
        background-image:
          linear-gradient(rgba(36,43,50,0.5) 1px, transparent 1px),
          linear-gradient(90deg, rgba(36,43,50,0.5) 1px, transparent 1px);
        background-size:44px 44px;
        pointer-events:none;
      }
      .auth-brand-content {
        position:relative;
        z-index:1;
        text-align:center;
        max-width:380px;
      }

      /* Logo */
      .auth-logo {
        display:inline-flex;
        align-items:center;
        gap:12px;
        font-family:'Manrope',sans-serif;
        font-size:1.5rem;
        font-weight:800;
        color:#FFFFFF;
        letter-spacing:-.025em;
        margin-bottom:32px;
        text-decoration:none;
      }
      .auth-logo-mark {
        width:42px; height:42px;
        background:rgba(0,214,143,0.12);
        border:1px solid rgba(0,214,143,0.28);
        border-radius:10px;
        display:flex; align-items:center; justify-content:center;
      }

      /* Headline */
      .auth-headline {
        font-family:'Manrope',sans-serif;
        font-size:clamp(1.9rem,3vw,2.6rem);
        font-weight:800;
        line-height:1.12;
        letter-spacing:-.04em;
        margin-bottom:20px;
      }
      .auth-headline .g { color:#00D68F; }

      .auth-tagline {
        color:#9AA5B1;
        font-size:.95rem;
        line-height:1.7;
        max-width:320px;
        margin:0 auto 40px;
      }

      /* Stats strip */
      .auth-stats {
        display:flex;
        gap:0;
        border:1px solid #242B32;
        border-radius:12px;
        overflow:hidden;
        background:#151A1F;
      }
      .auth-stat {
        flex:1;
        padding:18px 16px;
        text-align:center;
        border-right:1px solid #242B32;
      }
      .auth-stat:last-child { border-right:none; }
      .auth-stat-val {
        font-family:'Manrope',sans-serif;
        font-size:1.4rem;
        font-weight:800;
        color:#00D68F;
        letter-spacing:-.03em;
        line-height:1;
        margin-bottom:4px;
      }
      .auth-stat-lbl {
        font-size:.68rem;
        font-weight:700;
        color:#5C6773;
        text-transform:uppercase;
        letter-spacing:.08em;
      }

      /* Back link */
      .auth-back {
        position:absolute;
        top:24px; left:24px;
        display:inline-flex;
        align-items:center;
        gap:6px;
        font-size:.8rem;
        font-weight:600;
        color:#5C6773;
        text-decoration:none;
        transition:color .15s;
        z-index:2;
      }
      .auth-back:hover { color:#9AA5B1; }
      .auth-back svg { width:14px; height:14px; }

      /* ── RIGHT: Form side ── */
      .auth-form-side {
        display:flex;
        flex-direction:column;
        align-items:center;
        justify-content:center;
        padding:48px 36px;
        background:#0F1318;
        border-left:1px solid #1E252B;
        overflow-y:auto;
      }

      /* Card */
      .auth-card {
        width:100%;
        max-width:400px;
        animation:slideUp .45s cubic-bezier(.16,1,.3,1) both;
      }
      @keyframes slideUp {
        from { opacity:0; transform:translateY(18px); }
        to   { opacity:1; transform:translateY(0); }
      }

      .auth-card-title {
        font-family:'Manrope',sans-serif;
        font-size:1.5rem;
        font-weight:800;
        letter-spacing:-.03em;
        margin-bottom:6px;
      }
      .auth-card-sub {
        font-size:.88rem;
        color:#9AA5B1;
        margin-bottom:32px;
      }

      /* Form */
      .form-stack  { display:flex; flex-direction:column; gap:16px; }
      .form-group  { display:flex; flex-direction:column; gap:6px; }
      .form-label  {
        font-size:.75rem;
        font-weight:700;
        color:#9AA5B1;
        letter-spacing:.01em;
      }
      .form-input {
        width:100%;
        padding:11px 14px;
        background:#1E252B;
        border:1px solid #242B32;
        border-radius:8px;
        font-size:.9rem;
        color:#FFFFFF;
        font-family:'Inter',sans-serif;
        outline:none;
        transition:border-color .15s, background .15s, box-shadow .15s;
      }
      .form-input::placeholder { color:#5C6773; }
      .form-input:focus {
        border-color:#00D68F;
        background:#151A1F;
        box-shadow:0 0 0 3px rgba(0,214,143,0.10);
      }
      .password-field { position:relative; }
      .pw-toggle {
        position:absolute; right:11px; top:50%; transform:translateY(-50%);
        background:none; border:none; cursor:pointer;
        color:#5C6773; font-size:13px;
        transition:color .12s;
        padding:4px;
      }
      .pw-toggle:hover { color:#9AA5B1; }

      /* Submit button */
      .btn-submit {
        width:100%;
        padding:13px 24px;
        background:#00D68F;
        color:#0B0E11;
        border:none;
        border-radius:999px;
        font-size:.9rem;
        font-weight:800;
        font-family:'Manrope',sans-serif;
        cursor:pointer;
        margin-top:4px;
        transition:all .15s cubic-bezier(.16,1,.3,1);
        display:flex;
        align-items:center;
        justify-content:center;
        gap:8px;
      }
      .btn-submit:hover {
        background:#00A86B;
        transform:translateY(-1px);
        box-shadow:0 8px 24px rgba(0,214,143,.28);
      }
      .btn-submit:active { transform:scale(0.98); }

      /* Divider */
      .auth-divider {
        display:flex; align-items:center; gap:12px;
        font-size:.76rem; color:#5C6773;
        margin:20px 0;
      }
      .auth-divider::before, .auth-divider::after {
        content:''; flex:1; height:1px; background:#242B32;
      }

      /* Demo box */
      .demo-box {
        background:#151A1F;
        border:1px solid #242B32;
        border-radius:10px;
        padding:16px;
      }
      .demo-label {
        font-size:.68rem; font-weight:700; color:#5C6773;
        text-transform:uppercase; letter-spacing:.08em;
        margin-bottom:12px;
      }
      .demo-creds {
        display:grid; grid-template-columns:1fr 1fr;
        gap:8px; margin-bottom:12px;
      }
      .demo-key { font-size:.68rem; font-weight:700; color:#5C6773; text-transform:uppercase; letter-spacing:.06em; margin-bottom:2px; }
      .demo-val { font-family:'JetBrains Mono','Fira Code',monospace; font-size:.84rem; font-weight:600; color:#FFFFFF; }
      .btn-demo {
        width:100%; padding:8px 12px;
        background:rgba(0,214,143,0.07);
        border:1px solid rgba(0,214,143,0.2);
        border-radius:8px;
        font-size:.78rem; font-weight:700;
        color:#00D68F;
        cursor:pointer;
        transition:all .12s;
        font-family:'Inter',sans-serif;
      }
      .btn-demo:hover { background:rgba(0,214,143,0.14); }

      /* Footer link */
      .auth-footer-link {
        text-align:center;
        margin-top:24px;
        font-size:.84rem;
        color:#5C6773;
      }
      .auth-footer-link a {
        color:#00D68F;
        font-weight:700;
        text-decoration:none;
      }
      .auth-footer-link a:hover { color:#00A86B; }

      /* Flash messages */
      .flash {
        padding:11px 14px;
        border-radius:8px;
        font-size:.84rem;
        font-weight:500;
        margin-bottom:16px;
        border:1px solid transparent;
      }
      .flash-error {
        background:rgba(246,70,93,0.10);
        color:#F6465D;
        border-color:rgba(246,70,93,0.2);
      }
      .flash-success {
        background:rgba(0,214,143,0.10);
        color:#00D68F;
        border-color:rgba(0,214,143,0.2);
      }

      /* Responsive */
      @media (max-width:768px) {
        .auth-shell { grid-template-columns:1fr; }
        .auth-brand { min-height:220px; padding:40px 24px; }
        .auth-brand::after { display:none; }
        .auth-form-side { padding:36px 20px; border-left:none; border-top:1px solid #1E252B; }
      }
    </style>
</head>
<body>
<div class="auth-shell">

    <%-- LEFT: Brand panel --%>
    <div class="auth-brand">
        <a href="/" class="auth-back">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 12H5M12 5l-7 7 7 7"/></svg>
            Back to Home
        </a>
        <div class="auth-brand-content">
            <a href="/" class="auth-logo">
                <div class="auth-logo-mark">
                    <svg width="20" height="20" viewBox="0 0 28 28" fill="none">
                        <path d="M14 2L26 8.5V19.5L14 26L2 19.5V8.5L14 2Z" stroke="#00D68F" stroke-width="1.5" fill="rgba(0,214,143,.1)"/>
                        <text x="14" y="19" text-anchor="middle" fill="#00D68F" font-size="10" font-weight="800" font-family="Manrope">₿</text>
                    </svg>
                </div>
                CryptoForge
            </a>
            <h1 class="auth-headline">
                Trade Crypto.<br>
                <span class="g">Fast. Secure.</span><br>
                Now.
            </h1>
            <p class="auth-tagline">
                A high-performance exchange with real-time market data, ACID-safe order execution, and a seamless trading experience.
            </p>
            <div class="auth-stats">
                <div class="auth-stat">
                    <div class="auth-stat-val">7+</div>
                    <div class="auth-stat-lbl">Assets</div>
                </div>
                <div class="auth-stat">
                    <div class="auth-stat-val">Live</div>
                    <div class="auth-stat-lbl">Prices</div>
                </div>
                <div class="auth-stat">
                    <div class="auth-stat-val">$10K</div>
                    <div class="auth-stat-lbl">Balance</div>
                </div>
                <div class="auth-stat">
                    <div class="auth-stat-val">ACID</div>
                    <div class="auth-stat-lbl">Safe</div>
                </div>
            </div>
        </div>
    </div>

    <%-- RIGHT: Form --%>
    <div class="auth-form-side">
        <div class="auth-card">
            <h2 class="auth-card-title">Welcome back</h2>
            <p class="auth-card-sub">Sign in to continue trading</p>

            <c:if test="${not empty error}">
                <div class="flash flash-error">${error}</div>
            </c:if>
            <c:if test="${param.error != null}">
                <div class="flash flash-error">Invalid username or password. Please try again.</div>
            </c:if>
            <c:if test="${param.logout != null}">
                <div class="flash flash-success">You've been signed out successfully.</div>
            </c:if>

            <form method="POST" action="/login" class="form-stack" autocomplete="on">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="form-group">
                    <label class="form-label" for="username">Username</label>
                    <input type="text" id="username" name="username"
                           class="form-input" placeholder="your_username"
                           autocomplete="username" required autofocus>
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Password</label>
                    <div class="password-field">
                        <input type="password" id="password" name="password"
                               class="form-input" placeholder="••••••••"
                               autocomplete="current-password" required
                               style="padding-right:38px;">
                        <button type="button" class="pw-toggle" tabindex="-1"
                                onclick="var p=document.getElementById('password');p.type=p.type==='password'?'text':'password';">
                            👁
                        </button>
                    </div>
                </div>

                <button type="submit" class="btn-submit">
                    Sign In
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                </button>
            </form>



            <div class="auth-footer-link">
                Don't have an account? <a href="/register">Create account →</a>
            </div>
        </div>
    </div>
</div>
</body>
</html>
