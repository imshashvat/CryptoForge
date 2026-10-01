<%-- common/nav.jsp — CryptoForge Navigation v4 (Dark Green, CoinSwitch-inspired) --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String navUsername = (String) session.getAttribute("username");
    String navRole     = (String) session.getAttribute("role");
    String navInitial  = (navUsername != null && !navUsername.isEmpty())
                         ? String.valueOf(navUsername.charAt(0)).toUpperCase() : "?";
    String requestURI  = request.getRequestURI();
%>
<nav class="site-nav" id="siteNav">
    <div class="nav-inner">

        <%-- Logo --%>
        <a href="/dashboard" class="nav-logo">
            <div class="nav-logo-mark">
                <svg width="16" height="16" viewBox="0 0 28 28" fill="none">
                    <path d="M14 2L26 8.5V19.5L14 26L2 19.5V8.5L14 2Z"
                          stroke="#00D68F" stroke-width="1.5" fill="rgba(0,214,143,.15)"/>
                    <text x="14" y="19" text-anchor="middle" fill="#00D68F"
                          font-size="9" font-weight="800" font-family="Manrope">₿</text>
                </svg>
            </div>
            CryptoForge
        </a>

        <div class="nav-divider"></div>

        <%-- Center links --%>
        <div class="nav-links">
            <a href="/dashboard"    class="nav-link <%= requestURI.contains("dashboard")    ? "active" : "" %>">Markets</a>
            <a href="/portfolio"    class="nav-link <%= requestURI.contains("portfolio")    ? "active" : "" %>">Portfolio</a>
            <a href="/order"        class="nav-link <%= requestURI.contains("order")        ? "active" : "" %>">Trade</a>
            <a href="/transactions" class="nav-link <%= requestURI.contains("transactions") ? "active" : "" %>">History</a>
            <a href="/alerts"       class="nav-link <%= requestURI.contains("alerts")       ? "active" : "" %>" style="position:relative;">
                Alerts
                <span class="nav-badge" id="navAlertBadge" style="display:none;">0</span>
            </a>
            <a href="/leaderboard"  class="nav-link <%= requestURI.contains("leaderboard")  ? "active" : "" %>">Ranks</a>
            <% if ("ADMIN".equals(navRole)) { %>
            <a href="/admin" class="nav-link <%= requestURI.contains("admin") ? "active" : "" %>"
               style="color:var(--amber);">Admin</a>
            <% } %>
        </div>

        <div class="nav-spacer"></div>

        <%-- Right: User info + avatar --%>
        <div class="nav-actions">
            <button class="theme-toggle" id="themeToggle" onclick="window.CryptoForge&&window.CryptoForge.toggleTheme()" title="Toggle theme" style="margin-right:12px;">
                <svg id="themeIconSun" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon-sun" style="display:none;">
                    <circle cx="12" cy="12" r="4"/><line x1="12" y1="2" x2="12" y2="5"/><line x1="12" y1="19" x2="12" y2="22"/>
                    <line x1="4.22" y1="4.22" x2="6.34" y2="6.34"/><line x1="17.66" y1="17.66" x2="19.78" y2="19.78"/>
                    <line x1="2" y1="12" x2="5" y2="12"/><line x1="19" y1="12" x2="22" y2="12"/>
                    <line x1="4.22" y1="19.78" x2="6.34" y2="17.66"/><line x1="17.66" y1="6.34" x2="19.78" y2="4.22"/>
                </svg>
                <svg id="themeIconMoon" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon-moon" style="display:block;">
                    <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/>
                </svg>
            </button>
            <div class="nav-user">
                <div style="display:flex;flex-direction:column;align-items:flex-end;gap:1px;">
                    <span class="nav-username"><%= navUsername != null ? navUsername : "Guest" %></span>
                    <% if ("ADMIN".equals(navRole)) { %>
                    <span class="nav-role" style="color:var(--amber);">Admin</span>
                    <% } else { %>
                    <span class="nav-role">Trader</span>
                    <% } %>
                </div>
                <div class="nav-avatar" id="navAvatar" title="Account menu">
                    <%= navInitial %>
                </div>
            </div>
        </div>
    </div>
</nav>

<script>
(function(){
    /* Sticky shadow on scroll */
    var nav = document.getElementById('siteNav');
    if(!nav) return;
    window.addEventListener('scroll', function(){
        nav.classList.toggle('scrolled', window.scrollY > 10);
    }, {passive:true});
})();
</script>
