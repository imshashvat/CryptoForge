<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Price Alerts — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
    <style>
        .alert-direction-toggle {
            display:flex;
            background:var(--bg-subtle);
            border:1px solid var(--border);
            border-radius:var(--radius-sm);
            padding:3px;
            gap:3px;
        }
        .alert-dir-btn {
            flex:1;
            padding:7px 14px;
            border-radius:5px;
            border:none;
            font-size:13px;
            font-weight:600;
            cursor:pointer;
            background:transparent;
            color:var(--text-muted);
            transition:all var(--dur-fast);
        }
        .alert-dir-btn.above.active { background:var(--green); color:white; }
        .alert-dir-btn.below.active { background:var(--red);   color:white; }
    </style>
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>
<%!
    private static final DateTimeFormatter ALERT_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
%>

<div class="page-wrap animate-in">

    <div class="page-header">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="page-title">🔔 Price Alerts</h1>
                <p class="page-subtitle">Get notified when an asset reaches your target price</p>
            </div>
        </div>
    </div>

    <c:if test="${not empty success}">
        <div class="flash flash-success">✓ ${success}</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="flash flash-error">⚠ ${error}</div>
    </c:if>

    <div style="display:grid;grid-template-columns:1fr 360px;gap:var(--space-6);align-items:start;">

        <!-- LEFT: Active Alerts -->
        <div>
            <div style="font-family:var(--font-display);font-size:16px;font-weight:700;margin-bottom:var(--space-4);">Active Alerts</div>
            <c:choose>
                <c:when test="${empty alerts}">
                    <div class="empty-state card">
                        <div class="empty-state-icon">🔔</div>
                        <div class="empty-state-title">No active alerts</div>
                        <div class="empty-state-desc">Create your first price alert to be notified when a coin hits your target.</div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert-grid">
                        <c:forEach var="alert" items="${alerts}">
                            <div class="alert-card ${alert.triggered ? 'triggered' : ''}">
                                <div class="flex items-center justify-between" style="margin-bottom:var(--space-3);">
                                    <div class="flex items-center gap-3">
                                        <div class="coin-icon">${alert.assetCode.substring(0,1)}</div>
                                        <div>
                                            <div class="coin-name">${alert.assetCode}</div>
                                            <div class="coin-symbol">${alert.assetName}</div>
                                        </div>
                                    </div>
                                    <c:choose>
                                        <c:when test="${alert.triggered}">
                                            <span class="badge badge-green">✓ Triggered</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-blue">Active</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex items-center justify-between" style="margin-bottom:var(--space-2);">
                                    <span class="text-secondary" style="font-size:13px;">Target Price</span>
                                    <span class="price-mono font-bold">
                                        $<fmt:formatNumber value="${alert.targetPrice}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </span>
                                </div>
                                <div class="flex items-center justify-between">
                                    <span class="text-secondary" style="font-size:13px;">Direction</span>
                                    <span class="badge ${alert.direction == 'ABOVE' ? 'badge-green' : 'badge-red'}">
                                        ${alert.direction == 'ABOVE' ? '↑ Above' : '↓ Below'}
                                    </span>
                                </div>
                                <c:if test="${alert.triggered && alert.triggeredAt != null}">
                                    <div class="divider"></div>
                                    <div style="font-size:12px;color:var(--text-muted);">
                                        Triggered: <%
                                            Object alertObj = pageContext.getAttribute("alert");
                                            if (alertObj instanceof com.nietproject.cryptoforge.model.PriceAlert) {
                                                com.nietproject.cryptoforge.model.PriceAlert al =
                                                    (com.nietproject.cryptoforge.model.PriceAlert) alertObj;
                                                if (al.getTriggeredAt() != null)
                                                    out.print(al.getTriggeredAt().format(ALERT_FMT));
                                            }
                                        %>
                                    </div>
                                </c:if>
                                <c:if test="${!alert.triggered}">
                                    <div class="divider"></div>
                                    <form method="POST" action="/alerts/delete">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <input type="hidden" name="alertId" value="${alert.id}">
                                        <button type="submit" class="btn btn-ghost btn-sm btn-full" style="color:var(--red);">Delete</button>
                                    </form>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- RIGHT: Create Alert -->
        <div>
            <div class="card" style="position:sticky;top:calc(var(--nav-h) + var(--space-4));">
                <div style="font-family:var(--font-display);font-size:16px;font-weight:700;margin-bottom:var(--space-5);">Create Alert</div>
                <form method="POST" action="/alerts/create" class="auth-form-stack" style="gap:var(--space-4);">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <div class="form-group">
                        <label class="form-label">Asset</label>
                        <select name="assetCode" class="form-select" required>
                            <c:forEach var="a" items="${assets}">
                                <option value="${a.code}">${a.name} (${a.code})</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Target Price (USD)</label>
                        <div class="input-group">
                            <span class="input-prefix">$</span>
                            <input type="number" name="targetPrice" class="form-input mono with-prefix"
                                   step="0.000001" min="0.000001" placeholder="0.00" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Alert when price goes</label>
                        <div class="alert-direction-toggle">
                            <button type="button" class="alert-dir-btn above active"
                                    onclick="setDir('ABOVE',this)">↑ Above</button>
                            <button type="button" class="alert-dir-btn below"
                                    onclick="setDir('BELOW',this)">↓ Below</button>
                        </div>
                        <input type="hidden" name="direction" id="dirInput" value="ABOVE">
                    </div>
                    <button type="submit" class="btn btn-primary btn-full">Set Alert</button>
                </form>
            </div>
        </div>
    </div>
</div>

<script src="/assets/js/forge.js"></script>
<script>
function setDir(val, el) {
    document.getElementById('dirInput').value = val;
    document.querySelectorAll('.alert-dir-btn').forEach(function(b){ b.classList.remove('active'); });
    el.classList.add('active');
}
</script>
</body>
</html>
