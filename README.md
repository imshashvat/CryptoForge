# ⬡ CryptoForge
### Online Cryptocurrency Exchange Platform
**Advanced Java Programming (BCSAI0612) — B.Tech Third Year, NIET**

> A production-grade cryptocurrency exchange simulation. Users register, receive $10,000 mock USD, view live prices, place buy/sell orders, and track their portfolio — backed by MySQL with ACID guarantees.

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Deploy on Render](https://img.shields.io/badge/Deploy-Render-46E3B7)](https://render.com)
[![Deploy on Vercel](https://img.shields.io/badge/Deploy-Vercel-000)](https://vercel.com)

---

## 📚 Syllabus Coverage

| Unit | Topic | Module |
|------|-------|--------|
| **Unit I** | JDBC + Servlet API | `OrderService` (raw JDBC, transactions), `LoginAuditServlet`, `AuthFilter`, `SessionTrackingListener` |
| **Unit II** | JSP | `loginError.jsp` — all 7 implicit objects, scriptlet/expression/declaration tags |
| **Unit III** | Spring Core | `AppConfig.java` — `@Bean`, constructor DI, `@PostConstruct`/`@PreDestroy` in `MarketDataPoller` |
| **Unit IV** | Spring MVC/Boot | 8 REST controllers, auto-configured HikariCP/Jackson/Tomcat, `@SpringBootApplication` |
| **Unit V** | JPA | 5 entities with relationships, derived queries, explicit `@Query` JPQL |

---

## 🏗️ Architecture

```
Browser (JSP / React)
        │
        ▼
┌─────────────────────────────────┐
│   CryptoForge (Spring Boot)     │
│  ┌─────────────────────────┐    │
│  │ Servlet Layer (Unit I)  │    │  LoginAuditServlet
│  │ AuthFilter, SessionListener  │  (raw HttpServlet + JDBC)
│  ├─────────────────────────┤    │
│  │ JSP Views (Unit II)     │    │  loginError.jsp + implicit objects
│  ├─────────────────────────┤    │
│  │ Spring MVC (Unit IV)    │    │  8 @RestController endpoints
│  ├─────────────────────────┤    │
│  │ Spring Core (Unit III)  │    │  @Configuration, @Bean, lifecycle
│  ├─────────────────────────┤    │
│  │ Service Layer           │    │  OrderService (raw JDBC) + JPA services
│  └─────────────────────────┘    │
└───────────────┬─────────────────┘
                │ JDBC / JPA
         ┌──────▼──────┐
         │   MySQL 8   │
         └─────────────┘
```

---

## 🚀 Quick Start (Local)

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0 (or Docker)

### Option 1 — Docker (Easiest)
```bash
git clone https://github.com/imshashvat/CryptoForge.git
cd CryptoForge
docker compose up -d
# App: http://localhost:8080
# API: http://localhost:8080/actuator/health
```

### Option 2 — Manual
```bash
# 1. Create database
mysql -u root -p < src/main/resources/db/schema.sql
mysql -u root -p < src/main/resources/db/seed.sql

# 2. Set environment variables
export DB_PASSWORD=cryptoforge
export JWT_SECRET=YourSecretKeyAtLeast32Characters!

# 3. Run
mvn spring-boot:run

# 4. Open http://localhost:8080
```

### Default Credentials
| Username | Password | Role |
|----------|----------|------|
| `admin`  | `Admin@123` | ADMIN |

---

## 🌐 Deployment

### Marketing Website → Vercel (Static)
```bash
# Push to GitHub. Vercel auto-detects vercel.json and deploys index.html.
# Or: npx vercel --prod
```

### Spring Boot API → Render (Docker)
1. Push to GitHub
2. New Render Web Service → Connect repo
3. Environment → Add variables from `.env.example`
4. Render detects `render.yaml` and deploys automatically

### MySQL → Railway
1. New Railway project → Add MySQL plugin
2. Copy `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` to Render environment

---

## 📡 REST API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register + create wallets |
| `POST` | `/api/auth/login` | Login → JWT token |
| `GET`  | `/api/wallet` | User wallet balances |
| `GET`  | `/api/assets` | Live crypto prices |
| `POST` | `/api/orders` | Place BUY/SELL order |
| `GET`  | `/api/orders` | Order history |
| `GET`  | `/api/portfolio` | Portfolio + P&L |
| `GET`  | `/api/transactions` | Full ledger |
| `GET`  | `/actuator/health` | Health check (Render) |

---

## 🔑 Key Viva Points

| Question | Answer |
|----------|--------|
| "Show manual transaction management" | `OrderService.placeOrder()` — `setAutoCommit(false)` → `commit()` / `rollback()` |
| "Show CallableStatement" | `OrderService.getPortfolioValueViaProcedure()` → `sp_get_portfolio_value` |
| "Show Servlet lifecycle" | `AuthFilter` — `init()`, `doFilter()`, `destroy()` |
| "Show JSP implicit objects" | `loginError.jsp` — all 7 objects named explicitly |
| "Show Bean lifecycle" | `MarketDataPoller` — `@PostConstruct` starts thread, `@PreDestroy` stops it |
| "What does @SpringBootApplication compose?" | `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| "Why BigDecimal, not double?" | Floating-point precision loss — `0.1 + 0.2 ≠ 0.3` in IEEE 754 |
| "How are concurrent orders safe?" | `SELECT FOR UPDATE` pessimistic lock — DB-level serialization |

---

## 👥 Team

| Member | Module |
|--------|--------|
| **Shashvat Tripathi** | Auth, Spring Core Config, Spring MVC Controllers |
| **Member B** | Order Matching Engine (raw JDBC), Market Data Thread |
| **Member C** | JSP Views, JPA Entities, Portfolio Service |

**CSE, NIET (2024–2028) — V Semester, B.Tech Third Year**

---

## 📄 License

MIT — For educational use. Built for Advanced Java Programming (BCSAI0612).
