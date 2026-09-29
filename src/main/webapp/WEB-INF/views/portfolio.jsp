<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"   %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"    %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Portfolio — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>

<div class="page-wrap animate-in">

    <c:if test="${not empty success}">
        <div class="flash flash-success">✓ ${success}</div>
    </c:if>

    <!-- PORTFOLIO HEADER -->
    <div class="portfolio-header">
        <div>
            <div class="portfolio-total-label">Total Portfolio Value</div>
            <div class="portfolio-total-value">$<fmt:formatNumber value="${totalValue}" minFractionDigits="2" maxFractionDigits="2"/></div>
            <div style="margin-top:var(--space-3);display:flex;gap:var(--space-3);">
                <span class="badge badge-muted">Powered by CoinGecko</span>
                <span class="badge badge-green">● Live</span>
            </div>
        </div>

        <div class="portfolio-metrics">
            <div class="portfolio-metric">
                <div class="portfolio-metric-label">USD Balance</div>
                <div class="portfolio-metric-value">$<fmt:formatNumber value="${usdBalance}" minFractionDigits="2" maxFractionDigits="2"/></div>
            </div>
            <div class="portfolio-metric">
                <div class="portfolio-metric-label">Crypto Value</div>
                <div class="portfolio-metric-value">$<fmt:formatNumber value="${cryptoValue}" minFractionDigits="2" maxFractionDigits="2"/></div>
            </div>
            <div class="portfolio-metric">
                <div class="portfolio-metric-label">Total Invested</div>
                <div class="portfolio-metric-value">$<fmt:formatNumber value="${totalInvested}" minFractionDigits="2" maxFractionDigits="2"/></div>
            </div>
            <div class="portfolio-metric">
                <div class="portfolio-metric-label">Realized P&L</div>
                <div class="portfolio-metric-value <c:choose><c:when test='${totalRealized > 0}'>text-green</c:when><c:when test='${totalRealized < 0}'>text-red</c:when></c:choose>">
                    $<fmt:formatNumber value="${totalRealized}" minFractionDigits="2" maxFractionDigits="2"/>
                </div>
            </div>
        </div>

        <div class="chart-wrap">
            <canvas id="donutChart" style="width:180px;height:180px;"></canvas>
        </div>
    </div>

    <!-- STORED PROC BADGE -->
    <div class="flash flash-info" style="margin-bottom:var(--space-6);">
        ⚙ Crypto value calculated via stored procedure <code style="font-family:var(--font-mono);background:var(--accent-light);padding:1px 6px;border-radius:4px;">sp_get_portfolio_value()</code>
        via <code style="font-family:var(--font-mono);">CallableStatement</code>
    </div>

    <!-- HOLDINGS TABLE -->
    <div class="table-wrap">
        <div style="padding:var(--space-5) var(--space-6);display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid var(--border);">
            <span style="font-family:var(--font-display);font-size:16px;font-weight:700;">Your Holdings</span>
            <a href="/order" class="btn btn-primary btn-sm">+ Place Order</a>
        </div>
        <c:choose>
            <c:when test="${empty holdings}">
                <div class="empty-state">
                    <div class="empty-state-icon">₿</div>
                    <div class="empty-state-title">No holdings yet</div>
                    <div class="empty-state-desc">Place your first trade to start building your portfolio.</div>
                    <a href="/order" class="btn btn-primary" style="margin-top:var(--space-5);">Start Trading</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="scroll-x">
                    <table class="cf-table">
                        <thead>
                            <tr>
                                <th>Asset</th>
                                <th class="text-right">Quantity</th>
                                <th class="text-right">Current Price</th>
                                <th class="text-right">Market Value</th>
                                <th class="text-right">24h Change</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="h" items="${holdings}">
                                <tr>
                                    <td>
                                        <div class="coin-info">
                                            <div class="coin-icon">${h.code.substring(0,1)}</div>
                                            <div>
                                                <div class="coin-name">${h.name}</div>
                                                <div class="coin-symbol">${h.code}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-right price-mono">
                                        <fmt:formatNumber value="${h.quantity}" maxFractionDigits="8"/>
                                    </td>
                                    <td class="text-right price-mono">
                                        $<fmt:formatNumber value="${h.currentPrice}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td class="text-right price-mono font-bold">
                                        $<fmt:formatNumber value="${h.valueUsd}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td class="text-right">
                                        <span class="change-pill <c:choose><c:when test='${h.priceChange24h >= 0}'>change-up</c:when><c:otherwise>change-down</c:otherwise></c:choose>">
                                            <c:if test="${h.priceChange24h >= 0}">+</c:if><fmt:formatNumber value="${h.priceChange24h}" maxFractionDigits="2"/>%
                                        </span>
                                    </td>
                                    <td>
                                        <div class="flex gap-2">
                                            <a href="/order?asset=${h.code}" class="btn btn-sm btn-buy" style="width:56px;padding:4px 0;">Buy</a>
                                            <a href="/order?asset=${h.code}&type=SELL" class="btn btn-sm btn-sell" style="width:56px;padding:4px 0;">Sell</a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- USD WALLET -->
    <div class="card mt-6">
        <div class="flex items-center justify-between">
            <div>
                <div class="card-title">💵 USD Wallet</div>
                <div class="card-value">$<fmt:formatNumber value="${usdBalance}" minFractionDigits="2" maxFractionDigits="2"/></div>
                <div class="card-sub">Available for trading</div>
            </div>
            <a href="/order" class="btn btn-primary">Trade Now →</a>
        </div>
    </div>

</div>

<script src="/assets/js/forge.js"></script>
<script>
// Draw allocation donut
window.addEventListener('load', function() {
    var data = [
        { label: 'USD', value: ${usdBalance} },
        <c:forEach var="h" items="${holdings}">
        { label: '${h.code}', value: ${h.valueUsd} },
        </c:forEach>
    ].filter(function(d){ return d.value > 0; });
    if (window.CryptoForge && window.CryptoForge.drawDonut) {
        window.CryptoForge.drawDonut('donutChart', data);
    }
});
</script>
</body>
</html>
