package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PriceAlert — a user-defined alert that fires when an asset crosses a target price.
 */
@Entity
@Table(name = "price_alerts")
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "asset_code", nullable = false, length = 10)
    private String assetCode;

    @Column(name = "asset_name", length = 100)
    private String assetName;

    @Column(name = "target_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Direction direction; // ABOVE or BELOW

    @Column(nullable = false)
    private boolean triggered = false;

    @Column(name = "triggered_at")
    private LocalDateTime triggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum Direction { ABOVE, BELOW }

    // Getters & Setters
    public Long getId()                        { return id; }
    public Long getUserId()                    { return userId; }
    public String getAssetCode()               { return assetCode; }
    public String getAssetName()               { return assetName; }
    public BigDecimal getTargetPrice()         { return targetPrice; }
    public Direction getDirection()            { return direction; }
    public boolean isTriggered()               { return triggered; }
    public LocalDateTime getTriggeredAt()      { return triggeredAt; }
    public LocalDateTime getCreatedAt()        { return createdAt; }

    public void setUserId(Long userId)                     { this.userId = userId; }
    public void setAssetCode(String assetCode)             { this.assetCode = assetCode; }
    public void setAssetName(String assetName)             { this.assetName = assetName; }
    public void setTargetPrice(BigDecimal targetPrice)     { this.targetPrice = targetPrice; }
    public void setDirection(Direction direction)           { this.direction = direction; }
    public void setTriggered(boolean triggered)            { this.triggered = triggered; }
    public void setTriggeredAt(LocalDateTime triggeredAt)  { this.triggeredAt = triggeredAt; }
}
