<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"   %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"    %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trade — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
    <style>
        .order-type-toggle {
            display:flex;
            background:var(--bg-subtle);
            border:1px solid var(--border);
            border-radius:var(--radius-sm);
            padding:3px;
            gap:3px;
            width:100%;
            margin-bottom:var(--space-4);
        }
        .order-type-btn {
            flex:1;
            padding:8px;
            border-radius:5px;
            border:none;
            font-size:14px;
            font-weight:700;
            cursor:pointer;
            background:transparent;
            color:var(--text-muted);
            transition:all var(--dur-fast);
        }
        .order-type-btn.buy.active  { background:var(--green); color:white; }
        .order-type-btn.sell.active { background:var(--red);   color:white; }
    </style>
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>

<div class="page-wrap animate-in">

    <c:if test="${not empty success}">
        <div class="flash flash-success">✓ ${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flash flash-error">⚠ ${error}</div>
    </c:if>

    <div class="order-layout">

        <!-- LEFT: Asset list + chart area -->
        <div>
            <!-- Asset selector chips -->
            <div class="card mb-6">
                <div style="font-size:13px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:.06em;margin-bottom:var(--space-4);">Select Asset</div>
                <div class="asset-chips" id="assetChips">
                    <c:forEach var="a" items="${assets}">
                        <button type="button"
                                class="asset-chip <c:if test='${a.code == selectedAsset}'>active</c:if>"
                                data-asset="${a.code}"
                                data-price="${a.currentPriceUsd}"
                                data-name="${a.name}">
                            ${a.code}
                        </button>
                    </c:forEach>
                </div>
            </div>

            <!-- Selected asset price display -->
            <div class="card">
                <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:var(--space-4);">
                    <div>
                        <div style="font-size:13px;font-weight:600;color:var(--text-muted);margin-bottom:var(--space-1);" id="selectedAssetName">
                            <c:forEach var="a" items="${assets}"><c:if test="${a.code == selectedAsset}">${a.name} (${a.code})</c:if></c:forEach>
                        </div>
                        <div style="font-family:var(--font-mono);font-size:36px;font-weight:700;color:var(--text-primary);letter-spacing:-0.02em;"
                             id="assetCurrentPrice"
                             data-price="<c:forEach var='a' items='${assets}'><c:if test='${a.code == selectedAsset}'>${a.currentPriceUsd}</c:if></c:forEach>">
                            $<c:forEach var="a" items="${assets}"><c:if test="${a.code == selectedAsset}"><fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="2" maxFractionDigits="2"/></c:if></c:forEach>
                        </div>
                    </div>
                    <span class="badge badge-green" style="font-size:11px;">● Live Price</span>
                </div>

                <!-- All assets mini-table -->
                <div class="scroll-x" style="margin-top:var(--space-4);">
                    <table class="cf-table">
                        <thead>
                            <tr>
                                <th>Asset</th>
                                <th class="text-right">Price</th>
                                <th class="text-right">24h</th>
                                <th class="text-right">Your Balance</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="a" items="${assets}">
                                <tr style="cursor:pointer;" onclick="selectAsset('${a.code}','${a.currentPriceUsd}','${a.name}')">
                                    <td>
                                        <div class="coin-info">
                                            <div class="coin-icon" style="font-size:13px;">${a.code.substring(0,1)}</div>
                                            <div>
                                                <div class="coin-name">${a.name}</div>
                                                <div class="coin-symbol">${a.code}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-right price-mono" data-price="${a.code}">
                                        $<fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td class="text-right">
                                        <span class="change-pill <c:choose><c:when test='${a.priceChange24h >= 0}'>change-up</c:when><c:otherwise>change-down</c:otherwise></c:choose>">
                                            <c:if test="${a.priceChange24h >= 0}">+</c:if><fmt:formatNumber value="${a.priceChange24h}" maxFractionDigits="2"/>%
                                        </span>
                                    </td>
                                    <td class="text-right price-mono text-muted">—</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- RIGHT: Order Panel -->
        <div>
            <div class="order-panel">
                <div class="order-tabs">
                    <button id="buyTab" class="order-tab buy-tab active" type="button">Buy</button>
                    <button id="sellTab" class="order-tab sell-tab" type="button">Sell</button>
                </div>

                <form method="POST" action="/order" id="orderForm">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="orderType" id="orderTypeInput" value="BUY">
                    <input type="hidden" name="assetCode" id="assetCodeInput" value="${selectedAsset}">

                    <div class="order-body">

                        <!-- Available balance -->
                        <div style="background:var(--bg-subtle);border-radius:var(--radius-md);padding:var(--space-3) var(--space-4);display:flex;justify-content:space-between;align-items:center;">
                            <span style="font-size:12px;color:var(--text-muted);font-weight:600;">Available USD</span>
                            <span style="font-family:var(--font-mono);font-weight:700;color:var(--text-primary);">
                                $<fmt:formatNumber value="${usdBalance}" minFractionDigits="2" maxFractionDigits="2"/>
                            </span>
                        </div>

                        <!-- Quantity -->
                        <div class="form-group">
                            <label class="form-label" for="quantityInput">Quantity</label>
                            <div class="input-group">
                                <input type="number" id="quantityInput" name="quantity"
                                       class="form-input mono with-suffix"
                                       step="0.000001" min="0.000001"
                                       placeholder="0.00" required>
                                <span class="input-suffix" id="qtyUnit">${selectedAsset}</span>
                            </div>
                        </div>

                        <!-- Quick quantity buttons -->
                        <div style="display:flex;gap:var(--space-2);">
                            <button type="button" class="btn btn-ghost btn-sm" style="flex:1;" onclick="setQtyPercent(0.25)">25%</button>
                            <button type="button" class="btn btn-ghost btn-sm" style="flex:1;" onclick="setQtyPercent(0.5)">50%</button>
                            <button type="button" class="btn btn-ghost btn-sm" style="flex:1;" onclick="setQtyPercent(0.75)">75%</button>
                            <button type="button" class="btn btn-ghost btn-sm" style="flex:1;" onclick="setQtyPercent(1)">Max</button>
                        </div>

                        <!-- Order summary -->
                        <div class="order-summary">
                            <div class="order-summary-row">
                                <span class="label">Asset Price</span>
                                <span class="value" id="summaryPrice">
                                    $<c:forEach var="a" items="${assets}"><c:if test="${a.code == selectedAsset}"><fmt:formatNumber value="${a.currentPriceUsd}" minFractionDigits="2" maxFractionDigits="2"/></c:if></c:forEach>
                                </span>
                            </div>
                            <div class="order-summary-row">
                                <span class="label">Quantity</span>
                                <span class="value" id="summaryQty">—</span>
                            </div>
                            <div class="order-summary-row">
                                <span class="label">Fee (0.1%)</span>
                                <span class="value" id="estimatedFee">—</span>
                            </div>
                            <div class="order-summary-row total">
                                <span class="label" style="font-weight:700;color:var(--text-primary);">Total</span>
                                <span class="value" id="estimatedTotal">—</span>
                            </div>
                        </div>

                        <button type="submit" id="orderSubmitBtn" class="btn btn-buy btn-lg btn-full">
                            Buy Now
                        </button>

                        <div style="font-size:11px;color:var(--text-muted);text-align:center;">
                            Paper trading only — no real funds involved
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="/assets/js/forge.js"></script>
<script>
var currentPrice = parseFloat(document.getElementById('assetCurrentPrice').dataset.price) || 0;
var usdBal = ${usdBalance};

function selectAsset(code, price, name) {
    document.getElementById('assetCodeInput').value = code;
    document.getElementById('assetCurrentPrice').dataset.price = price;
    document.getElementById('assetCurrentPrice').textContent = '$' + parseFloat(price).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});
    document.getElementById('qtyUnit').textContent = code;
    document.getElementById('selectedAssetName').textContent = name + ' (' + code + ')';
    document.getElementById('summaryPrice').textContent = '$' + parseFloat(price).toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2});
    currentPrice = parseFloat(price);
    document.querySelectorAll('.asset-chip').forEach(function(c){c.classList.toggle('active', c.dataset.asset===code);});
    recalcOrder();
}

function setQtyPercent(pct) {
    var qty = document.getElementById('quantityInput');
    if (!currentPrice) return;
    var maxQty = (usdBal * pct) / currentPrice;
    qty.value = maxQty.toFixed(6);
    recalcOrder();
}

function recalcOrder() {
    var qty = parseFloat(document.getElementById('quantityInput').value) || 0;
    var total = qty * currentPrice;
    var fee   = total * 0.001;
    document.getElementById('summaryQty').textContent     = qty ? qty.toFixed(8) : '—';
    document.getElementById('estimatedTotal').textContent = total ? '$' + total.toLocaleString('en-US',{minimumFractionDigits:2,maximumFractionDigits:2}) : '—';
    document.getElementById('estimatedFee').textContent   = total ? '$' + fee.toFixed(4) : '—';
}
document.getElementById('quantityInput').addEventListener('input', recalcOrder);

// Chip click — also handled by forge.js but we supplement here
document.querySelectorAll('.asset-chip[data-asset]').forEach(function(chip) {
    chip.addEventListener('click', function() {
        selectAsset(chip.dataset.asset, chip.dataset.price, chip.dataset.name);
    });
});
</script>
</body>
</html>
