package com.nietproject.cryptoforge.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wallet> wallets;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    public User() {}

    // Builder pattern (manual — Lombok removed for JDK 25 compatibility)
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String username, email, passwordHash;
        private Role role = Role.USER;
        private boolean active = true;

        public Builder username(String v)      { this.username = v; return this; }
        public Builder email(String v)         { this.email = v; return this; }
        public Builder passwordHash(String v)  { this.passwordHash = v; return this; }
        public Builder role(Role v)            { this.role = v; return this; }
        public Builder active(boolean v)       { this.active = v; return this; }

        public User build() {
            User u = new User();
            u.username     = this.username;
            u.email        = this.email;
            u.passwordHash = this.passwordHash;
            u.role         = this.role;
            u.active       = this.active;
            return u;
        }
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                   { return id; }
    public String getUsername()           { return username; }
    public String getEmail()              { return email; }
    public String getPasswordHash()       { return passwordHash; }
    public Role getRole()                 { return role; }
    public boolean isActive()             { return active; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
    public List<Wallet> getWallets()      { return wallets; }
    public List<Order> getOrders()        { return orders; }
    public List<Transaction> getTransactions() { return transactions; }

    // Setters
    public void setId(Long id)                           { this.id = id; }
    public void setUsername(String username)             { this.username = username; }
    public void setEmail(String email)                   { this.email = email; }
    public void setPasswordHash(String passwordHash)     { this.passwordHash = passwordHash; }
    public void setRole(Role role)                       { this.role = role; }
    public void setActive(boolean active)                { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt)    { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)    { this.updatedAt = updatedAt; }

    // UserDetails interface
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword()              { return passwordHash; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return active; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return active; }

    public enum Role { USER, ADMIN }
}
