package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Version — Optimistic locking for concurrency safety
 * @ManyToOne — Entity Relationships
 * DECIMAL(20,8) — never FLOAT for financial values (viva answer!)
 */
@Entity
@Table(name = "wallets",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "currency_code"}))
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "currency_code", nullable = false, length = 10)
    private String currencyCode;

    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Wallet() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private User user;
        private String currencyCode;
        private BigDecimal balance = BigDecimal.ZERO;

        public Builder user(User v)           { this.user = v; return this; }
        public Builder currencyCode(String v) { this.currencyCode = v; return this; }
        public Builder balance(BigDecimal v)  { this.balance = v; return this; }

        public Wallet build() {
            Wallet w = new Wallet();
            w.user         = this.user;
            w.currencyCode = this.currencyCode;
            w.balance      = this.balance;
            return w;
        }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                 { return id; }
    public User getUser()               { return user; }
    public String getCurrencyCode()     { return currencyCode; }
    public BigDecimal getBalance()      { return balance; }
    public Long getVersion()            { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id)                        { this.id = id; }
    public void setUser(User user)                    { this.user = user; }
    public void setCurrencyCode(String currencyCode)  { this.currencyCode = currencyCode; }
    public void setBalance(BigDecimal balance)         { this.balance = balance; }
    public void setVersion(Long version)              { this.version = version; }
    public void setCreatedAt(LocalDateTime v)         { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v)         { this.updatedAt = v; }
}
