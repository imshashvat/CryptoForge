<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"   %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"    %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Markets — CryptoForge</title>
    <meta name="description" content="Live cryptocurrency market prices, charts and instant order execution on CryptoForge.">
    <link rel="stylesheet" href="/assets/css/forge.css">
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>

<!-- TICKER BAR -->
<div class="ticker-bar">
    <div class="ticker-track" id="tickerTrack">
        <c:forEach var="a" items="${assets}">
            <div class="ticker-item" data-code="${a.code}">
                <span class="ticker-sym">${a.code}</span>
                <span class="ticker-price">
                    <c:choose>
                        <c:when test="${a.currentPriceUsd >= 1}">
                            $<fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="2" maxFractionDigits="2"/>
                        </c:when>
                        <c:otherwise>
                            $<fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="4" maxFractionDigits="6"/>
                        </c:otherwise>
                    </c:choose>
                </span>
                <span class="ticker-change <c:choose><c:when test='${a.priceChange24h >= 0}'>change-up</c:when><c:otherwise>change-down</c:otherwise></c:choose>">
                    <c:if test="${a.priceChange24h >= 0}">+</c:if><fmt:formatNumber value="${a.priceChange24h}" maxFractionDigits="2"/>%
                </span>
            </div>
        </c:forEach>
    </div>
</div>

<div class="page-wrap animate-in">

    <!-- STATS ROW -->
    <div class="stats-row mb-6">
        <div class="stat-card">
            <div class="stat-label">Portfolio Value</div>
            <div class="stat-value">$<fmt:formatNumber value="${usdBalance + cryptoValue}" minFractionDigits="2" maxFractionDigits="2"/></div>
            <div class="stat-change up">All assets combined</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">USD Balance</div>
            <div class="stat-value">$<fmt:formatNumber value="${usdBalance}" minFractionDigits="2" maxFractionDigits="2"/></div>
            <div class="stat-change" style="color:var(--text-muted);">Available to trade</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Crypto Holdings</div>
            <div class="stat-value">$<fmt:formatNumber value="${cryptoValue}" minFractionDigits="2" maxFractionDigits="2"/></div>
            <div class="stat-change" style="color:var(--text-muted);">Market value</div>
        </div>
        <div class="stat-card">
            <div class="stat-label">Total Trades</div>
            <div class="stat-value">${totalTrades}</div>
            <div class="stat-change" style="color:var(--text-muted);">Completed orders</div>
        </div>
    </div>

    <!-- MARKET TABLE -->
    <div class="market-table-wrap">
        <div class="market-table-header">
            <span class="market-table-title">Live Markets</span>
            <span style="font-size:11px;color:var(--text-muted);font-weight:600;background:var(--green-light);color:var(--green);padding:3px 10px;border-radius:9999px;">
                ● Live
            </span>
            <div class="market-search">
                <svg class="market-search-icon" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input type="text" id="marketSearch" class="market-search-input" placeholder="Search assets...">
            </div>
        </div>

        <div class="scroll-x">
            <table class="cf-table">
                <thead>
                    <tr>
                        <th style="width:40px;">#</th>
                        <th data-sort="name">Asset <span class="sort-icon">↕</span></th>
                        <th data-sort="currentPriceUsd">Price <span class="sort-icon">↕</span></th>
                        <th data-sort="priceChange24h">24h Change <span class="sort-icon">↕</span></th>
                        <th data-sort="marketCapUsd">Market Cap <span class="sort-icon">↕</span></th>
                        <th>7d Chart</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody id="marketTbody">
                    <c:forEach var="a" items="${assets}" varStatus="s">
                        <%-- Hidden data row for JS to pick up --%>
                        <tr data-asset='{"code":"${a.code}","name":"${a.name}","currentPriceUsd":${a.currentPriceUsd},"priceChange24h":${a.priceChange24h},"marketCapUsd":${a.marketCapUsd}}' style="display:none;"></tr>
                    </c:forEach>
                    <%-- Server-rendered fallback (shown without JS) --%>
                    <c:forEach var="a" items="${assets}" varStatus="s">
                        <tr class="js-hide" data-code="${a.code}">
                            <td class="text-muted" style="font-size:12px;">${s.index + 1}</td>
                            <td>
                                <div class="coin-info">
                                    <div class="coin-icon" style="font-size:14px;">${a.code.substring(0,1)}</div>
                                    <div>
                                        <div class="coin-name">${a.name}</div>
                                        <div class="coin-symbol">${a.code}</div>
                                    </div>
                                </div>
                            </td>
                            <td class="price-mono" data-price="${a.code}">
                                <c:choose>
                                    <c:when test="${a.currentPriceUsd >= 1}">$<fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="2" maxFractionDigits="2"/></c:when>
                                    <c:otherwise>$<fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="4" maxFractionDigits="6"/></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <span class="change-pill <c:choose><c:when test='${a.priceChange24h >= 0}'>change-up</c:when><c:otherwise>change-down</c:otherwise></c:choose>">
                                    <c:if test="${a.priceChange24h >= 0}">+</c:if><fmt:formatNumber value="${a.priceChange24h}" maxFractionDigits="2"/>%
                                </span>
                            </td>
                            <td class="price-mono text-secondary">
                                <c:choose>
                                    <c:when test="${a.marketCapUsd >= 1000000000000}">$<fmt:formatNumber value="${a.marketCapUsd / 1000000000000}" maxFractionDigits="2"/>T</c:when>
                                    <c:when test="${a.marketCapUsd >= 1000000000}">$<fmt:formatNumber value="${a.marketCapUsd / 1000000000}" maxFractionDigits="2"/>B</c:when>
                                    <c:when test="${a.marketCapUsd >= 1000000}">$<fmt:formatNumber value="${a.marketCapUsd / 1000000}" maxFractionDigits="2"/>M</c:when>
                                    <c:otherwise>$<fmt:formatNumber value="${a.marketCapUsd}" maxFractionDigits="0"/></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <canvas style="width:80px;height:32px;" data-spark="${a.code}" data-change="${a.priceChange24h}" data-price-val="${a.currentPriceUsd}"></canvas>
                            </td>
                            <td>
                                <div class="flex gap-2">
                                    <a href="/order?asset=${a.code}" class="btn btn-sm btn-buy" style="width:60px;padding:5px 0;">Buy</a>
                                    <a href="/order?asset=${a.code}&type=SELL" class="btn btn-sm btn-sell" style="width:60px;padding:5px 0;">Sell</a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

</div>

<script src="/assets/js/forge.js"></script>
<script>
(function () {
  // Hide server-rendered rows (JS will take over)
  // Actually keep them visible, just draw sparklines on them
  document.querySelectorAll('[data-spark]').forEach(function (canvas) {
    var change = parseFloat(canvas.dataset.change) || 0;
    var price  = parseFloat(canvas.dataset.priceVal) || 100;
    var color  = change >= 0 ? '#16A34A' : '#DC2626';
    var data   = [];
    var cur    = price;
    for (var i = 0; i < 20; i++) {
      cur = cur * (1 + (change / 100 / 20) + (Math.random() - 0.5) * 0.006);
      data.push(cur);
    }
    if (window.CryptoForge) window.CryptoForge.drawSparkline(canvas, data, color);
  });

  // Alert badge
  fetch('/api/alerts/count').then(function(r){return r.json();}).then(function(n){
    if (n > 0) {
      var b = document.getElementById('navAlertBadge');
      if (b) { b.style.display=''; b.textContent=n; }
    }
  }).catch(function(){});
})();
</script>
</body>
</html>
