package com.nietproject.cryptoforge.dto;

import java.math.BigDecimal;

public class AuthResponse {
    private String     token;
    private String     username;
    private String     email;
    private String     role;
    private BigDecimal usdBalance;
    private String     message;

    public AuthResponse() {}

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String token, username, email, role, message;
        private BigDecimal usdBalance;
        public Builder token(String v)        { this.token = v; return this; }
        public Builder username(String v)     { this.username = v; return this; }
        public Builder email(String v)        { this.email = v; return this; }
        public Builder role(String v)         { this.role = v; return this; }
        public Builder usdBalance(BigDecimal v){ this.usdBalance = v; return this; }
        public Builder message(String v)      { this.message = v; return this; }
        public AuthResponse build() {
            AuthResponse r = new AuthResponse();
            r.token = token; r.username = username; r.email = email;
            r.role = role; r.usdBalance = usdBalance; r.message = message;
            return r;
        }
    }

    public String getToken()          { return token; }
    public String getUsername()       { return username; }
    public String getEmail()          { return email; }
    public String getRole()           { return role; }
    public BigDecimal getUsdBalance() { return usdBalance; }
    public String getMessage()        { return message; }
    public void setToken(String v)          { this.token = v; }
    public void setUsername(String v)       { this.username = v; }
    public void setEmail(String v)          { this.email = v; }
    public void setRole(String v)           { this.role = v; }
    public void setUsdBalance(BigDecimal v) { this.usdBalance = v; }
    public void setMessage(String v)        { this.message = v; }
}
