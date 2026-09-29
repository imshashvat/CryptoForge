<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en" data-theme="dark">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Transaction History — CryptoForge</title>
    <link rel="stylesheet" href="/assets/css/forge.css">
</head>
<body class="app-shell">
<%@ include file="common/nav.jsp" %>

<%!
    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
%>

<div class="page-wrap animate-in">

    <div class="page-header">
        <div class="flex items-center justify-between">
            <div>
                <h1 class="page-title">Transaction History</h1>
                <p class="page-subtitle">Complete ledger of all your buy and sell orders</p>
            </div>
            <a href="/order" class="btn btn-primary">+ New Trade</a>
        </div>
    </div>

    <!-- FILTER BAR -->
    <div class="filter-bar">
        <div class="filter-tabs">
            <button class="filter-tab active" data-filter="ALL">All</button>
            <button class="filter-tab" data-filter="BUY">Buy</button>
            <button class="filter-tab" data-filter="SELL">Sell</button>
        </div>
        <div style="margin-left:auto;font-size:13px;color:var(--text-muted);">
            <c:choose>
                <c:when test="${empty transactions}">No transactions yet</c:when>
                <c:otherwise>${fn:length(transactions)} transactions</c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- TRANSACTION TABLE -->
    <div class="table-wrap">
        <c:choose>
            <c:when test="${empty transactions}">
                <div class="empty-state">
                    <div class="empty-state-icon">📋</div>
                    <div class="empty-state-title">No transactions yet</div>
                    <div class="empty-state-desc">Your trade history will appear here once you place your first order.</div>
                    <a href="/order" class="btn btn-primary" style="margin-top:var(--space-5);">Place First Trade</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="scroll-x">
                    <table class="cf-table">
                        <thead>
                            <tr>
                                <th>Date & Time</th>
                                <th>Type</th>
                                <th>Asset</th>
                                <th class="text-right">Quantity</th>
                                <th class="text-right">Price</th>
                                <th class="text-right">Total Value</th>
                                <th class="text-right">Balance After</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="tx" items="${transactions}">
                                <tr class="txn-row" data-type="${tx.type}">

                                    <td class="text-secondary" style="white-space:nowrap;font-size:13px;">

                                        <%
                                           /* Scriptlet: safely format LocalDateTime using DateTimeFormatter */
                                           Object txObj = pageContext.getAttribute("tx");
                                           if (txObj instanceof com.nietproject.cryptoforge.model.Transaction) {
                                               java.time.LocalDateTime dt =
                                                   ((com.nietproject.cryptoforge.model.Transaction) txObj).getCreatedAt();
                                               out.print(dt != null ? dt.format(DT_FMT) : "—");
                                           } else {
                                               out.print("—");
                                           }
                                        %>
                                    </td>
                                    <td>
                                        <span class="badge <c:choose><c:when test='${tx.type == \"BUY\"}'>badge-green</c:when><c:otherwise>badge-red</c:otherwise></c:choose>">
                                            ${tx.type}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="coin-info">
                                            <div class="coin-icon" style="font-size:13px;">${tx.asset.code.substring(0,1)}</div>
                                            <div>
                                                <div class="coin-name">${tx.asset.name}</div>
                                                <div class="coin-symbol">${tx.asset.code}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td class="text-right price-mono">
                                        <fmt:formatNumber value="${tx.quantity}" maxFractionDigits="8"/>
                                    </td>
                                    <td class="text-right price-mono">
                                        $<fmt:formatNumber value="${tx.priceUsd}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td class="text-right price-mono font-bold">
                                        $<fmt:formatNumber value="${tx.totalValueUsd}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td class="text-right price-mono text-secondary">
                                        $<fmt:formatNumber value="${tx.balanceAfter}" minFractionDigits="2" maxFractionDigits="2"/>
                                    </td>
                                    <td>
                                        <span class="badge badge-green">Completed</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<script src="/assets/js/forge.js"></script>
</body>
</html>
