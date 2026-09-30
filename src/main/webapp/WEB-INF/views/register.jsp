<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — CryptoForge</title>
    <meta name="description" content="Create a free CryptoForge account and start paper trading cryptocurrency with $10,000 USD.">
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
      .auth-shell {
        display:grid;
        grid-template-columns:1fr 1fr;
        width:100%; min-height:100vh;
      }

      /* ── LEFT brand panel ── */
      .auth-brand {
        position:relative; overflow:hidden;
        display:flex; flex-direction:column;
        align-items:center; justify-content:center;
        padding:56px 48px;
        background:#0B0E11;
      }
      .auth-brand::before {
        content:''; position:absolute;
        top:-200px; right:-100px;
        width:700px; height:700px;
        background:radial-gradient(circle,rgba(0,214,143,0.18) 0%,transparent 60%);
        pointer-events:none;
      }
      .auth-brand::after {
        content:''; position:absolute; inset:0;
        background-image:
          linear-gradient(rgba(36,43,50,0.5) 1px,transparent 1px),
          linear-gradient(90deg,rgba(36,43,50,0.5) 1px,transparent 1px);
        background-size:44px 44px;
        pointer-events:none;
      }
      .auth-brand-content {
        position:relative; z-index:1;
        text-align:center; max-width:380px;
      }
      .auth-logo {
        display:inline-flex; align-items:center; gap:12px;
        font-family:'Manrope',sans-serif;
        font-size:1.5rem; font-weight:800;
        color:#FFFFFF; letter-spacing:-.025em;
        margin-bottom:32px; text-decoration:none;
      }
      .auth-logo-mark {
        width:42px; height:42px;
        background:rgba(0,214,143,0.12);
        border:1px solid rgba(0,214,143,0.28);
        border-radius:10px;
        display:flex; align-items:center; justify-content:center;
      }
      .auth-headline {
        font-family:'Manrope',sans-serif;
        font-size:clamp(1.9rem,3vw,2.6rem);
        font-weight:800; line-height:1.12;
        letter-spacing:-.04em; margin-bottom:20px;
      }
      .auth-headline .g { color:#00D68F; }
      .auth-tagline {
        color:#9AA5B1; font-size:.95rem; line-height:1.7;
        max-width:320px; margin:0 auto 40px;
      }

      /* Features list */
      .auth-features { text-align:left; }
      .auth-feature {
        display:flex; align-items:flex-start; gap:12px;
        padding:12px 0; border-bottom:1px solid #1E252B;
      }
      .auth-feature:last-child { border-bottom:none; }
      .auth-feature-ico {
        width:34px; height:34px; flex-shrink:0;
        background:#0F3D2E;
        border:1px solid rgba(0,214,143,.2);
        border-radius:8px;
        display:flex; align-items:center; justify-content:center;
        margin-top:1px;
      }
      .auth-feature-ico svg { width:15px; height:15px; stroke:#00D68F; stroke-width:2; fill:none; }
      .auth-feature-title { font-weight:700; font-size:.88rem; color:#FFFFFF; margin-bottom:2px; }
      .auth-feature-desc  { font-size:.78rem; color:#5C6773; line-height:1.5; }

      /* Back link */
      .auth-back {
        position:absolute; top:24px; left:24px;
        display:inline-flex; align-items:center; gap:6px;
        font-size:.8rem; font-weight:600; color:#5C6773;
        text-decoration:none; transition:color .15s; z-index:2;
      }
      .auth-back:hover { color:#9AA5B1; }
      .auth-back svg { width:14px; height:14px; }

      /* ── RIGHT form side ── */
      .auth-form-side {
        display:flex; flex-direction:column;
        align-items:center; justify-content:center;
        padding:48px 36px;
        background:#0F1318;
        border-left:1px solid #1E252B;
        overflow-y:auto;
      }
      .auth-card {
        width:100%; max-width:400px;
        animation:slideUp .45s cubic-bezier(.16,1,.3,1) both;
      }
      @keyframes slideUp {
        from{opacity:0;transform:translateY(18px)}
        to{opacity:1;transform:translateY(0)}
      }
      .auth-card-title {
        font-family:'Manrope',sans-serif;
        font-size:1.5rem; font-weight:800;
        letter-spacing:-.03em; margin-bottom:6px;
      }
      .auth-card-sub {
        font-size:.88rem; color:#9AA5B1; margin-bottom:28px;
      }

      /* Form elements */
      .form-stack  { display:flex; flex-direction:column; gap:14px; }
      .form-group  { display:flex; flex-direction:column; gap:5px; }
      .form-label  { font-size:.75rem; font-weight:700; color:#9AA5B1; letter-spacing:.01em; }
      .form-input {
        width:100%; padding:11px 14px;
        background:#1E252B; border:1px solid #242B32;
        border-radius:8px; font-size:.9rem; color:#FFFFFF;
        font-family:'Inter',sans-serif; outline:none;
        transition:border-color .15s,background .15s,box-shadow .15s;
      }
      .form-input::placeholder { color:#5C6773; }
      .form-input:focus {
        border-color:#00D68F; background:#151A1F;
        box-shadow:0 0 0 3px rgba(0,214,143,0.10);
      }
      .form-hint  { font-size:.72rem; color:#5C6773; }
      .form-error { font-size:.72rem; color:#F6465D; }

      .password-field { position:relative; }
      .pw-toggle {
        position:absolute; right:11px; top:50%; transform:translateY(-50%);
        background:none; border:none; cursor:pointer;
        color:#5C6773; font-size:13px; padding:4px; transition:color .12s;
      }
      .pw-toggle:hover { color:#9AA5B1; }

      /* Password strength bar */
      .strength-bar {
        height:3px; background:#242B32;
        border-radius:999px; overflow:hidden; margin-top:6px;
      }
      .strength-fill { height:100%; border-radius:999px; transition:all .25s; width:0; }

      /* Submit */
      .btn-submit {
        width:100%; padding:13px 24px;
        background:#00D68F; color:#0B0E11;
        border:none; border-radius:999px;
        font-size:.9rem; font-weight:800;
        font-family:'Manrope',sans-serif;
        cursor:pointer; margin-top:4px;
        transition:all .15s cubic-bezier(.16,1,.3,1);
        display:flex; align-items:center; justify-content:center; gap:8px;
      }
      .btn-submit:hover {
        background:#00A86B; transform:translateY(-1px);
        box-shadow:0 8px 24px rgba(0,214,143,.28);
      }
      .btn-submit:active { transform:scale(.98); }
      .btn-submit:disabled { opacity:.6; cursor:not-allowed; transform:none; box-shadow:none; }

      .auth-footer-link {
        text-align:center; margin-top:22px;
        font-size:.84rem; color:#5C6773;
      }
      .auth-footer-link a { color:#00D68F; font-weight:700; text-decoration:none; }
      .auth-footer-link a:hover { color:#00A86B; }

      .flash {
        padding:11px 14px; border-radius:8px;
        font-size:.84rem; font-weight:500;
        margin-bottom:14px; border:1px solid transparent;
      }
      .flash-error   { background:rgba(246,70,93,.10);  color:#F6465D; border-color:rgba(246,70,93,.2); }
      .flash-success { background:rgba(0,214,143,.10);  color:#00D68F; border-color:rgba(0,214,143,.2); }

      #errorBanner { display:none; }

      @media(max-width:768px) {
        .auth-shell { grid-template-columns:1fr; }
        .auth-brand { min-height:240px; padding:40px 24px; }
        .auth-brand::after { display:none; }
        .auth-features { display:none; }
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
                Start Trading.<br>
                <span class="g">Zero Risk.</span><br>
                Free.
            </h1>
            <p class="auth-tagline">
                Get $10,000 virtual USD instantly. Trade live market prices with no real money at risk.
            </p>
            <div class="auth-features">
                <div class="auth-feature">
                    <div class="auth-feature-ico">
                        <svg viewBox="0 0 24 24"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                    </div>
                    <div>
                        <div class="auth-feature-title">Real-Time Market Prices</div>
                        <div class="auth-feature-desc">Live prices refreshed every 15 seconds from global crypto markets.</div>
                    </div>
                </div>
                <div class="auth-feature">
                    <div class="auth-feature-ico">
                        <svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                    </div>
                    <div>
                        <div class="auth-feature-title">ACID-Safe Order Engine</div>
                        <div class="auth-feature-desc">Pessimistic locking ensures your balances are always accurate.</div>
                    </div>
                </div>
                <div class="auth-feature">
                    <div class="auth-feature-ico">
                        <svg viewBox="0 0 24 24"><path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                    </div>
                    <div>
                        <div class="auth-feature-title">Bank-Level Security</div>
                        <div class="auth-feature-desc">JWT auth and BCrypt password hashing on every account.</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <%-- RIGHT: Form --%>
    <div class="auth-form-side">
        <div class="auth-card">
            <h2 class="auth-card-title">Create account</h2>
            <p class="auth-card-sub">Get started with $10,000 USD instantly — free</p>

            <div class="flash flash-error" id="errorBanner"></div>
            <c:if test="${not empty error}">
                <div class="flash flash-error">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="flash flash-success">${success}</div>
            </c:if>

            <form method="POST" action="/api/auth/register" class="form-stack" autocomplete="off" id="registerForm">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="form-group">
                    <label class="form-label" for="username">Username</label>
                    <input type="text" id="username" name="username"
                           class="form-input" placeholder="your_username"
                           pattern="[a-zA-Z0-9_]{3,20}"
                           title="3–20 characters, letters, numbers and underscores only"
                           required autofocus>
                    <span class="form-hint">3–20 characters, letters and numbers only</span>
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">Email address</label>
                    <input type="email" id="email" name="email"
                           class="form-input" placeholder="you@example.com"
                           autocomplete="email" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="passwordInput">Password</label>
                    <div class="password-field">
                        <input type="password" id="passwordInput" name="password"
                               class="form-input" placeholder="Min. 8 characters"
                               minlength="8" required style="padding-right:38px;">
                        <button type="button" class="pw-toggle" tabindex="-1"
                                onclick="var p=document.getElementById('passwordInput');p.type=p.type==='password'?'text':'password';">
                            👁
                        </button>
                    </div>
                    <div class="strength-bar">
                        <div class="strength-fill" id="strengthFill"></div>
                    </div>
                    <span class="form-hint" id="strengthLabel">Use uppercase, numbers and symbols</span>
                </div>

                <button type="submit" class="btn-submit" id="submitBtn">
                    Create Free Account
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
                </button>
            </form>

            <div class="auth-footer-link">
                Already have an account? <a href="/login">Sign in →</a>
            </div>
        </div>
    </div>
</div>

<script>
/* Password strength meter */
document.getElementById('passwordInput').addEventListener('input', function(){
    var val = this.value;
    var score = 0;
    if(val.length >= 8)  score++;
    if(/[A-Z]/.test(val)) score++;
    if(/[0-9]/.test(val)) score++;
    if(/[^A-Za-z0-9]/.test(val)) score++;
    var fill  = document.getElementById('strengthFill');
    var label = document.getElementById('strengthLabel');
    var colors = ['#F6465D','#F0B90B','#00A86B','#00D68F'];
    var labels = ['Too weak','Fair','Good','Strong'];
    fill.style.width  = (score * 25) + '%';
    fill.style.background = colors[Math.max(0,score-1)] || '#F6465D';
    label.textContent = score > 0 ? labels[score-1] : 'Use uppercase, numbers and symbols';
});

/* Register form AJAX */
document.getElementById('registerForm').addEventListener('submit', function(e){
    e.preventDefault();
    var btn  = document.getElementById('submitBtn');
    btn.textContent = 'Creating account…';
    btn.disabled = true;
    var data = new URLSearchParams(new FormData(this));
    fetch('/api/auth/register', {
        method:'POST', body:data,
        headers:{'Content-Type':'application/x-www-form-urlencoded'}
    })
    .then(function(r){ return r.json(); })
    .then(function(res){
        if(res.success || res.token){
            window.location = '/login?registered=1';
        } else {
            var banner = document.getElementById('errorBanner');
            banner.textContent = res.message || 'Registration failed. Please try again.';
            banner.style.display = 'block';
            btn.innerHTML = 'Create Free Account <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>';
            btn.disabled = false;
        }
    })
    .catch(function(){
        var banner = document.getElementById('errorBanner');
        banner.textContent = 'Network error. Please try again.';
        banner.style.display = 'block';
        btn.innerHTML = 'Create Free Account <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>';
        btn.disabled = false;
    });
});
</script>
</body>
</html>
