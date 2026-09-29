package com.nietproject.cryptoforge.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Asset code is required")
    @Size(max = 10, message = "Asset code must be at most 10 characters")
    private String assetCode;

    @NotBlank(message = "Order type must be BUY or SELL")
    @Pattern(regexp = "BUY|SELL", message = "Order type must be BUY or SELL")
    private String orderType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    public OrderRequest() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String assetCode;
        private String orderType;
        private BigDecimal quantity;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder assetCode(String assetCode) { this.assetCode = assetCode; return this; }
        public Builder orderType(String orderType) { this.orderType = orderType; return this; }
        public Builder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }

        public OrderRequest build() {
            OrderRequest request = new OrderRequest();
            request.userId = this.userId;
            request.assetCode = this.assetCode;
            request.orderType = this.orderType;
            request.quantity = this.quantity;
            return request;
        }
    }

    public Long getUserId()       { return userId; }
    public String getAssetCode()  { return assetCode; }
    public String getOrderType()  { return orderType; }
    public BigDecimal getQuantity(){ return quantity; }
    public void setUserId(Long v)       { this.userId = v; }
    public void setAssetCode(String v)  { this.assetCode = v; }
    public void setOrderType(String v)  { this.orderType = v; }
    public void setQuantity(BigDecimal v){ this.quantity = v; }
}
