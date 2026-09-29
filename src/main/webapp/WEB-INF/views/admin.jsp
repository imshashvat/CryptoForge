<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
    <script>(function(){var t=localStorage.getItem('cf-theme')||'light';document.documentElement.setAttribute('data-theme',t);})();</script>
    <style>
        .admin-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:var(--space-4); margin-bottom:var(--space-8); }
        .admin-stat { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--radius-lg); padding:var(--space-5); box-shadow:var(--shadow-xs); }
        .admin-stat-label { font-size:11px; font-weight:700; color:var(--text-muted); text-transform:uppercase; letter-spacing:.06em; margin-bottom:8px; }
        .admin-stat-val { font-family:var(--font-mono); font-size:24px; font-weight:700; color:var(--text-primary); }
        .user-table-wrap { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--radius-lg); overflow:hidden; margin-bottom:var(--space-8); box-shadow:var(--shadow-xs); }
        .table-header { padding:var(--space-4) var(--space-6); border-bottom:1px solid var(--border); display:flex; align-items:center; justify-content:space-between; background:var(--bg-subtle); }
        .user-actions { display:flex; gap:6px; flex-wrap:wrap; }
        .btn-danger { background:var(--red-light); color:var(--red); border:1px solid var(--red-mid); border-radius:var(--radius-sm); padding:4px 10px; font-size:12px; font-weight:700; cursor:pointer; transition:all 0.15s; }
        .btn-danger:hover { opacity:0.8; }
        .btn-success-sm { background:var(--green-light); color:var(--green); border:1px solid var(--green-mid); border-radius:var(--radius-sm); padding:4px 10px; font-size:12px; font-weight:700; cursor:pointer; transition:all 0.15s; }
        .btn-warn { background:var(--amber-light); color:var(--amber); border:1px solid #fde68a; border-radius:var(--radius-sm); padding:4px 10px; font-size:12px; font-weight:700; cursor:pointer; transition:all 0.15s; }
        .badge-admin { display:inline-block; background:var(--amber-light); color:var(--amber); border-radius:var(--radius-full); padding:2px 8px; font-size:11px; font-weight:700; }
        .badge-user  { display:inline-block; background:var(--bg-subtle); color:var(--text-muted); border-radius:var(--radius-full); padding:2px 8px; font-size:11px; font-weight:600; }
        .badge-banned{ display:inline-block; background:var(--red-light); color:var(--red); border-radius:var(--radius-full); padding:2px 8px; font-size:11px; font-weight:700; }
        .balance-modal { display:none; position:fixed; inset:0; background:rgba(0,0,0,0.5); z-index:1000; align-items:center; justify-content:center; backdrop-filter:blur(4px); }
        .balance-modal.open { display:flex; }
        .modal-card { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--radius-xl); padding:var(--space-8); width:400px; box-shadow:var(--shadow-xl); }
        .modal-card h3 { font-family:var(--font-display); font-size:18px; font-weight:700; margin-bottom:var(--space-5); color:var(--text-primary); }
        .txn-feed { max-height:320px; overflow-y:auto; }
        .txn-row { display:flex; align-items:center; justify-content:space-between; padding:10px var(--space-5); border-bottom:1px solid var(--border); font-size:13px; }
        .assets-live { display:grid; grid-template-columns:repeat(auto-fill,minmax(200px,1fr)); gap:var(--space-4); }
        .asset-live-card { background:var(--bg-surface); border:1px solid var(--border); border-radius:var(--radius-md); padding:var(--space-4); box-shadow:var(--shadow-xs); }
        @media(max-width:1024px){ .admin-grid{ grid-template-columns:1fr 1fr; } }
        @media(max-width:600px) { .admin-grid{ grid-template-columns:1fr; } }
    </style>
</head>
<body class="app-shell">

    <%@ include file="common/nav.jsp" %>

    <%-- Balance Modal --%>
    <div class="balance-modal" id="balanceModal">
        <div class="modal-card">
            <h3>Adjust User Balance</h3>
            <form action="/admin/balance/0" method="post" id="balanceForm">
                <div style="margin-bottom:var(--space-md);">
                    <label class="form-label">User: <strong id="modalUsername">—</strong></label>
                </div>
                <div style="margin-bottom:var(--space-md);">
                    <label class="form-label">Amount (USD)</label>
                    <input type="number" name="amount" min="1" step="0.01" class="form-control" placeholder="e.g. 1000.00" required>
                </div>
                <div style="display:flex; gap:8px; margin-bottom:var(--space-lg);">
                    <button type="submit" name="action" value="credit" class="btn btn-primary" style="flex:1;">+ Credit</button>
                    <button type="submit" name="action" value="debit" class="btn btn-outline" style="flex:1; color:var(--red); border-color:var(--red);">− Debit</button>
                </div>
                <button type="button" onclick="closeModal()" class="btn btn-outline" style="width:100%;">Cancel</button>
            </form>
        </div>
    </div>

    <div class="page-container">

        <div class="page-header">
            <div style="display:flex; align-items:center; justify-content:space-between;">
                <div>
                    <h1 class="page-title">⚙ Admin Control Panel</h1>
                    <p class="page-subtitle">Platform management — logged in as <strong style="color:var(--gold);">${adminUsername}</strong></p>
                </div>
            </div>
        </div>

        <%-- Flash messages --%>
        <c:if test="${not empty adminSuccess}"><div class="alert alert-success">✓ ${adminSuccess}</div></c:if>
        <c:if test="${not empty adminError}"><div class="alert alert-error">⚠ ${adminError}</div></c:if>

        <%-- Stats Row --%>
        <div class="admin-grid">
            <div class="admin-stat">
                <p class="card-label">Total Users</p>
                <p class="card-value cyan">${totalUsers}</p>
            </div>
            <div class="admin-stat">
                <p class="card-label">Active Users</p>
                <p class="card-value green">${activeUsers}</p>
            </div>
            <div class="admin-stat">
                <p class="card-label">Banned Users</p>
                <p class="card-value red">${bannedUsers}</p>
            </div>
            <div class="admin-stat">
                <p class="card-label">Total Orders</p>
                <p class="card-value gold">${totalOrders}</p>
            </div>
            <div class="admin-stat" style="grid-column: span 2;">
                <p class="card-label">Platform USD Pool</p>
                <p class="card-value gold">$<fmt:formatNumber value="${totalUsd}" type="number" minFractionDigits="2" maxFractionDigits="2"/></p>
                <p style="font-size:0.72rem; color:var(--text-dim); margin-top:4px;">Total USD across all wallets</p>
            </div>
            <div class="admin-stat" style="grid-column: span 2;">
                <p class="card-label">Live Assets</p>
                <div class="assets-live" style="margin-top:8px;">
                    <c:forEach var="a" items="${assets}" end="4">
                        <div class="asset-live-card">
                            <div style="font-weight:700; color:var(--text-primary); font-size:0.85rem;">${a.code}</div>
                            <div style="font-family:var(--font-mono); color:var(--gold); font-size:0.9rem;">
                                $<fmt:formatNumber value="${a.currentPriceUsd}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                            </div>
                            <div style="font-size:0.7rem; color:${a.priceChange24h >= 0 ? 'var(--green)' : 'var(--red)'};">
                                ${a.priceChange24h >= 0 ? '▲' : '▼'}
                                <fmt:formatNumber value="${a.priceChange24h >= 0 ? a.priceChange24h : a.priceChange24h * -1}" maxFractionDigits="2"/>%
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>

        <%-- Users Table --%>
        <div class="user-table-wrap">
            <div class="table-header">
                <h2 style="font-family:var(--font-display); font-size:1rem; font-weight:700;">User Management</h2>
                <span style="font-size:0.75rem; color:var(--text-dim);">${totalUsers} registered users</span>
            </div>
            <table class="forge-table">
                <thead>
                    <tr>
                        <th>#ID</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Joined</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="user" items="${allUsers}">
                        <tr>
                            <td style="font-family:var(--font-mono); color:var(--text-dim); font-size:0.8rem;">#${user.id}</td>
                            <td style="font-weight:600; color:var(--text-primary);">${user.username}</td>
                            <td style="color:var(--text-secondary); font-size:0.85rem;">${user.email}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.role.name() == 'ADMIN'}">
                                        <span class="badge-admin">ADMIN</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-user">USER</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.active}">
                                        <span style="color:var(--green); font-size:0.8rem; font-weight:600;">● Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-banned">BANNED</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="font-size:0.8rem; color:var(--text-dim);">
                                ${user.createdAt}
                            </td>
                            <td>
                                <div class="user-actions">
                                    <%-- Balance Adjust --%>
                                    <button class="btn-warn" onclick="openBalance(${user.id}, '${user.username}')">
                                        $ Balance
                                    </button>

                                    <%-- Ban / Unban --%>
                                    <c:choose>
                                        <c:when test="${user.active}">
                                            <form action="/admin/ban/${user.id}" method="post" style="display:inline;"
                                                  onsubmit="return confirm('Ban ${user.username}?')">
                                                <button type="submit" class="btn-danger">⛔ Ban</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="/admin/unban/${user.id}" method="post" style="display:inline;">
                                                <button type="submit" class="btn-success-sm">✓ Unban</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>

                                    <%-- Reset Portfolio --%>
                                    <form action="/admin/reset/${user.id}" method="post" style="display:inline;"
                                          onsubmit="return confirm('Reset ALL crypto holdings for ${user.username}? This cannot be undone.')">
                                        <button type="submit" class="btn-danger">🔄 Reset</button>
                                    </form>

                                    <%-- Promote / Demote --%>
                                    <c:choose>
                                        <c:when test="${user.role.name() == 'USER'}">
                                            <form action="/admin/promote/${user.id}" method="post" style="display:inline;"
                                                  onsubmit="return confirm('Promote ${user.username} to ADMIN?')">
                                                <button type="submit" class="btn-warn">▲ Promote</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="/admin/demote/${user.id}" method="post" style="display:inline;"
                                                  onsubmit="return confirm('Demote ${user.username} to USER?')">
                                                <button type="submit" class="btn-danger">▼ Demote</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <%-- Recent Platform Transactions --%>
        <div class="user-table-wrap">
            <div class="table-header">
                <h2 style="font-family:var(--font-display); font-size:1rem; font-weight:700;">Recent Platform Transactions</h2>
                <span style="font-size:0.75rem; color:var(--text-dim);">Last 50 trades</span>
            </div>
            <div class="txn-feed">
                <c:choose>
                    <c:when test="${empty recentTxns}">
                        <div style="padding:var(--space-xl); text-align:center; color:var(--text-dim);">No transactions yet.</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="txn" items="${recentTxns}">
                            <div class="txn-row">
                                <div style="display:flex; align-items:center; gap:10px;">
                                    <span class="badge ${txn.type.name() == 'BUY' ? 'badge-buy' : 'badge-sell'}" style="min-width:50px; text-align:center;">
                                        <span class="badge-dot"></span>${txn.type}
                                    </span>
                                    <div>
                                        <div style="font-weight:600; color:var(--text-primary); font-size:0.85rem;">
                                            ${txn.asset != null ? txn.asset.name : '?'} (${txn.asset != null ? txn.asset.code : '?'})
                                        </div>
                                        <div style="font-size:0.72rem; color:var(--text-dim);">
                                            User #${txn.user != null ? txn.user.id : '?'} · ${txn.createdAt}
                                        </div>
                                    </div>
                                </div>
                                <div style="text-align:right;">
                                    <div style="font-family:var(--font-mono); font-weight:700; color:${txn.type.name() == 'BUY' ? 'var(--red)' : 'var(--green)'};">
                                        ${txn.type.name() == 'BUY' ? '−' : '+'}$<fmt:formatNumber value="${txn.totalValueUsd}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                    </div>
                                    <div style="font-size:0.72rem; color:var(--text-dim);">
                                        <fmt:formatNumber value="${txn.quantity}" maxFractionDigits="8"/> @ $<fmt:formatNumber value="${txn.priceUsd}" maxFractionDigits="2"/>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>

<script src="/assets/js/forge.js"></script>
<script>
    function openBalance(userId, username) {
        document.getElementById('modalUsername').textContent = username;
        document.getElementById('balanceForm').action = '/admin/balance/' + userId;
        document.getElementById('balanceModal').classList.add('open');
    }
    function closeModal() {
        document.getElementById('balanceModal').classList.remove('open');
    }
    // Close modal on backdrop click
    document.getElementById('balanceModal').addEventListener('click', function(e) {
        if (e.target === this) closeModal();
    });
</script>
</body>
</html>
