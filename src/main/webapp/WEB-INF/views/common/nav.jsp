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
            <div class="nav-user">
                <div style="display:flex;flex-direction:column;align-items:flex-end;gap:1px;">
                    <span class="nav-username"><%= navUsername != null ? navUsername : "Guest" %></span>
                    <% if ("ADMIN".equals(navRole)) { %>
                    <span class="nav-role" style="color:var(--amber);">Admin</span>
                    <% } else { %>
                    <span class="nav-role">Trader</span>
                    <% } %>
                </div>
                <div class="nav-avatar" title="Sign out" onclick="window.location='/logout'">
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
