package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "current_price_usd", nullable = false, precision = 20, scale = 8)
    private BigDecimal currentPriceUsd = BigDecimal.ZERO;

    @Column(name = "price_change_24h", precision = 10, scale = 4)
    private BigDecimal priceChange24h = BigDecimal.ZERO;

    @Column(name = "market_cap_usd", precision = 30, scale = 2)
    private BigDecimal marketCapUsd = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Asset() {}

    @PrePersist @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters
    public String getCode()                  { return code; }
    public String getName()                  { return name; }
    public BigDecimal getCurrentPriceUsd()   { return currentPriceUsd; }
    public BigDecimal getPriceChange24h()    { return priceChange24h; }
    public BigDecimal getMarketCapUsd()      { return marketCapUsd; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }

    // Setters
    public void setCode(String code)                         { this.code = code; }
    public void setName(String name)                         { this.name = name; }
    public void setCurrentPriceUsd(BigDecimal v)             { this.currentPriceUsd = v; }
    public void setPriceChange24h(BigDecimal v)              { this.priceChange24h = v; }
    public void setMarketCapUsd(BigDecimal v)                { this.marketCapUsd = v; }
    public void setUpdatedAt(LocalDateTime v)                { this.updatedAt = v; }
}
