<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leaderboard — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>

<div class="page-wrap animate-in">

    <div class="page-header">
        <h1 class="page-title">🏆 Leaderboard</h1>
        <p class="page-subtitle">Top traders ranked by total portfolio value</p>
    </div>

    <!-- PODIUM (top 3) -->
    <div id="podiumSection" style="margin-bottom:var(--space-10);">
        <div class="podium" id="podium">
            <!-- Filled by JS from /api/leaderboard -->
            <div style="text-align:center;color:var(--text-muted);font-size:13px;padding:var(--space-8);">Loading rankings...</div>
        </div>
    </div>

    <!-- FULL RANKINGS TABLE -->
    <div class="table-wrap">
        <div style="padding:var(--space-5) var(--space-6);border-bottom:1px solid var(--border);">
            <span style="font-family:var(--font-display);font-size:16px;font-weight:700;">All Rankings</span>
        </div>
        <div class="scroll-x">
            <table class="cf-table">
                <thead>
                    <tr>
                        <th style="width:60px;">Rank</th>
                        <th>Trader</th>
                        <th class="text-right">Portfolio Value</th>
                        <th class="text-right">USD Balance</th>
                        <th class="text-right">Trades</th>
                    </tr>
                </thead>
                <tbody id="leaderboardTbody">
                    <tr><td colspan="5" class="text-center text-muted" style="padding:var(--space-8);">Loading...</td></tr>
                </tbody>
            </table>
        </div>
    </div>
</div>

<script src="/assets/js/forge.js"></script>
<script>
function medal(rank) {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return '#' + rank;
}
function fmtMoney(v) {
    return '$' + parseFloat(v).toLocaleString('en-US', {minimumFractionDigits:2, maximumFractionDigits:2});
}
fetch('/api/leaderboard')
    .then(function(r){ return r.json(); })
    .then(function(data) {
        // Podium
        var podium = document.getElementById('podium');
        var top3 = data.slice(0, 3);
        var order = top3.length >= 3 ? [1, 0, 2] : top3.map(function(_,i){ return i; });
        podium.innerHTML = '';
        order.forEach(function(idx) {
            var p = top3[idx];
            if (!p) return;
            var rank = idx + 1;
            var div = document.createElement('div');
            div.className = 'podium-place podium-' + rank;
            div.innerHTML = '<div class="podium-avatar">' + p.username.charAt(0).toUpperCase() + '</div>' +
                '<div style="font-weight:700;font-size:14px;color:var(--text-primary);margin-bottom:4px;">' + p.username + '</div>' +
                '<div style="font-family:var(--font-mono);font-size:13px;color:var(--text-secondary);">' + fmtMoney(p.totalValue) + '</div>' +
                '<div class="podium-bar"></div>';
            podium.appendChild(div);
        });
        // Table
        var tbody = document.getElementById('leaderboardTbody');
        tbody.innerHTML = '';
        data.forEach(function(p, i) {
            var tr = document.createElement('tr');
            tr.innerHTML = '<td><span style="font-size:18px;">' + medal(i+1) + '</span></td>' +
                '<td><div class="flex items-center gap-3"><div class="nav-avatar" style="width:32px;height:32px;font-size:12px;flex-shrink:0;">' + p.username.charAt(0).toUpperCase() + '</div><span style="font-weight:600;">' + p.username + '</span></div></td>' +
                '<td class="text-right price-mono font-bold">' + fmtMoney(p.totalValue) + '</td>' +
                '<td class="text-right price-mono text-secondary">' + fmtMoney(p.usdBalance || 0) + '</td>' +
                '<td class="text-right price-mono text-secondary">' + (p.tradeCount || 0) + '</td>';
            tbody.appendChild(tr);
        });
    })
    .catch(function() {
        document.getElementById('leaderboardTbody').innerHTML =
            '<tr><td colspan="5" class="text-center text-muted" style="padding:var(--space-8);">Could not load rankings.</td></tr>';
    });
</script>
</body>
</html>
