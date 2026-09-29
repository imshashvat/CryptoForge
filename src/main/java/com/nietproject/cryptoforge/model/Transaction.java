package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions",
       indexes = @Index(name = "idx_tx_user_id", columnList = "user_id"))
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Order.OrderType type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_code", nullable = false)
    private Asset asset;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal priceUsd;

    @Column(name = "total_value_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalValueUsd;

    @Column(name = "fee_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal feeUsd = BigDecimal.ZERO;

    @Column(name = "balance_after", nullable = false, precision = 20, scale = 8)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                   { return id; }
    public Order getOrder()               { return order; }
    public User getUser()                 { return user; }
    public Order.OrderType getType()      { return type; }
    public Asset getAsset()               { return asset; }
    public BigDecimal getQuantity()       { return quantity; }
    public BigDecimal getPriceUsd()       { return priceUsd; }
    public BigDecimal getTotalValueUsd()  { return totalValueUsd; }
    public BigDecimal getFeeUsd()         { return feeUsd; }
    public BigDecimal getBalanceAfter()   { return balanceAfter; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    // Setters
    public void setId(Long id)                      { this.id = id; }
    public void setOrder(Order order)               { this.order = order; }
    public void setUser(User user)                  { this.user = user; }
    public void setType(Order.OrderType v)          { this.type = v; }
    public void setAsset(Asset asset)               { this.asset = asset; }
    public void setQuantity(BigDecimal v)           { this.quantity = v; }
    public void setPriceUsd(BigDecimal v)           { this.priceUsd = v; }
    public void setTotalValueUsd(BigDecimal v)      { this.totalValueUsd = v; }
    public void setFeeUsd(BigDecimal v)             { this.feeUsd = v; }
    public void setBalanceAfter(BigDecimal v)       { this.balanceAfter = v; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }
}
