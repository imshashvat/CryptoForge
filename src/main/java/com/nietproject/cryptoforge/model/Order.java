package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
       indexes = {
           @Index(name = "idx_orders_user_id",    columnList = "user_id"),
           @Index(name = "idx_orders_created_at", columnList = "created_at")
       })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_code", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price_at_order", nullable = false, precision = 20, scale = 8)
    private BigDecimal priceAtOrder;

    @Column(name = "total_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Order() {}

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                 { return id; }
    public User getUser()               { return user; }
    public Asset getAsset()             { return asset; }
    public OrderType getOrderType()     { return orderType; }
    public BigDecimal getQuantity()     { return quantity; }
    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
    public BigDecimal getTotalValue()   { return totalValue; }
    public OrderStatus getStatus()      { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setUser(User user)                    { this.user = user; }
    public void setAsset(Asset asset)                 { this.asset = asset; }
    public void setOrderType(OrderType v)             { this.orderType = v; }
    public void setQuantity(BigDecimal v)             { this.quantity = v; }
    public void setPriceAtOrder(BigDecimal v)         { this.priceAtOrder = v; }
    public void setTotalValue(BigDecimal v)           { this.totalValue = v; }
    public void setStatus(OrderStatus v)              { this.status = v; }
    public void setCreatedAt(LocalDateTime v)         { this.createdAt = v; }

    public enum OrderType   { BUY, SELL }
    public enum OrderStatus { PENDING, COMPLETED, FAILED, CANCELLED }
}
