package com.nietproject.cryptoforge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResult {
    private Long         orderId;
    private String       status;
    private String       message;
    private String       assetCode;
    private String       orderType;
    private BigDecimal   quantity;
    private BigDecimal   priceAtExecution;
    private BigDecimal   totalValue;
    private BigDecimal   newUsdBalance;
    private BigDecimal   newAssetBalance;
    private LocalDateTime executedAt;

    public OrderResult() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private Long orderId; private String status, message, assetCode, orderType;
        private BigDecimal quantity, priceAtExecution, totalValue, newUsdBalance, newAssetBalance;
        private LocalDateTime executedAt;
        public Builder orderId(Long v)              { this.orderId = v; return this; }
        public Builder status(String v)             { this.status = v; return this; }
        public Builder message(String v)            { this.message = v; return this; }
        public Builder assetCode(String v)          { this.assetCode = v; return this; }
        public Builder orderType(String v)          { this.orderType = v; return this; }
        public Builder quantity(BigDecimal v)       { this.quantity = v; return this; }
        public Builder priceAtExecution(BigDecimal v){ this.priceAtExecution = v; return this; }
        public Builder totalValue(BigDecimal v)     { this.totalValue = v; return this; }
        public Builder newUsdBalance(BigDecimal v)  { this.newUsdBalance = v; return this; }
        public Builder newAssetBalance(BigDecimal v){ this.newAssetBalance = v; return this; }
        public Builder executedAt(LocalDateTime v)  { this.executedAt = v; return this; }
        public OrderResult build() {
            OrderResult r = new OrderResult();
            r.orderId = orderId; r.status = status; r.message = message;
            r.assetCode = assetCode; r.orderType = orderType; r.quantity = quantity;
            r.priceAtExecution = priceAtExecution; r.totalValue = totalValue;
            r.newUsdBalance = newUsdBalance; r.newAssetBalance = newAssetBalance;
            r.executedAt = executedAt; return r;
        }
    }

    public Long getOrderId()                { return orderId; }
    public String getStatus()               { return status; }
    public String getMessage()              { return message; }
    public String getAssetCode()            { return assetCode; }
    public String getOrderType()            { return orderType; }
    public BigDecimal getQuantity()         { return quantity; }
    public BigDecimal getPriceAtExecution() { return priceAtExecution; }
    public BigDecimal getTotalValue()       { return totalValue; }
    public BigDecimal getNewUsdBalance()    { return newUsdBalance; }
    public BigDecimal getNewAssetBalance()  { return newAssetBalance; }
    public LocalDateTime getExecutedAt()    { return executedAt; }
}
