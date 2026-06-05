# Worker Attendance & Overtime Settlement Engine

A Spring Boot backend that tracks daily worker attendance on construction sites and calculates / settles overtime pay. Built on an existing Task-Manager codebase to demonstrate extension of a real production project.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| ORM | JPA / Hibernate 6.4 |
| Database | PostgreSQL (Supabase via PgBouncer — port 6543) |
| Cache | Redis (Memurai locally; any Redis-compatible server) |
| Security | Spring Security + JWT |
| Build | Maven 3.9 |

---

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL (Supabase recommended) — pooler URL, port 6543
- Redis (Memurai on Windows or `redis-server` on Linux/Mac), default port 6379 or custom

---

## Setup

### 1. Clone and navigate

```bash
git clone https://github.com/RiteshBhardwaj999/Worker-Attendance-Overtime-Settlement-Engine.git
cd Worker-Attendance-Overtime-Settlement-Engine/backend
```

### 2. Create `backend/.env`

This file is gitignored and must be created locally. Never commit it.

```
PGHOST=<your-supabase-pooler-host>
PGPORT=6543
PGDATABASE=postgres
PGUSER=<your-supabase-user>
PGPASSWORD=<your-supabase-password>
JWT_SECRET=<a-random-string-of-at-least-32-chars>
PORT=8080
REDIS_HOST=127.0.0.1
REDIS_PORT=6380
```

**Supabase connection note**: Use the **Transaction Pooler** URL (port 6543, not 5432). The JDBC URL includes `?sslmode=require&prepareThreshold=0` — the `prepareThreshold=0` disables server-side prepared statements, which PgBouncer in transaction mode cannot reuse across connections.

### 3. Run

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

For staging (Hikari pool tuned for Supabase idle timeouts):

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=staging
```

---

## API Overview

All endpoints except `/api/auth/**` and `/actuator/**` require a Bearer JWT token.

### Auth

```
POST /api/auth/signup      — register a user
POST /api/auth/login       — returns { token }
```

### Workers

```
POST   /api/workers                  — create worker
GET    /api/workers                  — list all
GET    /api/workers/{id}             — get by id
PUT    /api/workers/{id}             — update (name, designation, dailyWageRate, active)
```

Worker payload:
```json
{
  "name": "Ramesh Kumar",
  "phone": "9876543210",
  "designation": "MASON",
  "dailyWageRate": 800.00
}
```
Designations: `MASON`, `ELECTRICIAN`, `PLUMBER`, `SUPERVISOR`, `HELPER`

### Sites

```
POST /api/sites             — create site
GET  /api/sites             — list all
PUT  /api/sites/{id}        — update
```

### Attendance

```
POST /api/attendance/clock-in                                       — clock in
POST /api/attendance/clock-out                                      — clock out
GET  /api/attendance/active                                         — currently on-site (Redis)
GET  /api/attendance/log?workerId=&from=&to=&page=0&size=20         — paginated log
```

Clock-in payload (`clockInTime` optional — defaults to now):
```json
{
  "workerId": "<uuid>",
  "siteId": "<uuid>",
  "clockInTime": "2025-05-01T08:00:00"
}
```

### Overtime

```
GET  /api/overtime/summary/{workerId}?month=2025-05    — monthly summary + minimum wage ref
POST /api/overtime/settle/{workerId}?month=2025-05     — mark all pending entries settled
```

### Health

```
GET /actuator/health        — DB + Redis liveness
```

---

## Business Rules

- Standard shift = 8 h. Overtime = hours beyond 8.
- OT rate: first 2 h at **1.5x** hourly rate, beyond 2 h at **2x** hourly rate.
- Hourly rate = `dailyWageRate / 8`.
- Monthly OT cap = **60 h** per worker. Attendance is recorded fully; the payable `OvertimeEntry` is trimmed to the remaining headroom.
- Shifts > 16 h set `flagged = true` on the `AttendanceLog` for manual review.
- A `@Scheduled` sweeper runs every 15 min to auto-flag stale open shifts (> 16 h, no clock-out).
- Settlement is blocked for the current and future months.

---

## Ticket Fixes (LF-201 to LF-205)

| Ticket | Root cause | Fix |
|---|---|---|
| LF-201 | CORS allowed-origins hardcoded `*` | Externalized to `app.cors.allowed-origins`; OPTIONS preflight permitted before security chain |
| LF-202 | Redis outage crashed the app | `CacheErrorHandler` swallows cache failures; all manual Redis ops wrapped in try/catch; `/active` falls back to DB |
| LF-203 | EAGER fetch + no pagination = N+1 | `@ManyToOne` changed to LAZY; `JOIN FETCH` JPQL query with `Page`; `/log` returns `PagedResponse` |
| LF-204 | Non-transactional settlement loop + inline SMS | `settle()` wrapped in `@Transactional`; `saveAll()` replaces per-entry saves; SMS fires via `@TransactionalEventListener(AFTER_COMMIT)` |
| LF-205 | External HTTP call inside `@Transactional` held DB connection; no Hikari tuning | `getSummary()` no longer opens a transaction; minimum-wage call runs before any DB access; `RestTemplate` has 3s connect / 5s read timeouts; `application-staging.yml` tunes Hikari `max-lifetime` (4 min < Supabase 5-min idle cutoff) |

---

## Design Tradeoffs

**DB-polling sweeper vs Redis keyspace events**: Redis key-expiry notifications are best-effort and can be dropped under load. A `@Scheduled` DB query for stale open attendance is simpler, always consistent, and survives Redis restarts.

**`overtimeRateApplied` snapshot**: The hourly rate is captured at clock-out time so future wage changes do not silently rewrite historical payout amounts.

**Supabase transaction pooler**: PgBouncer in transaction mode (port 6543) cannot reuse server-side prepared statements across connections, so `prepareThreshold=0` disables them. `max-lifetime` is set below Supabase's 5-minute idle timeout to prevent "connection already closed" errors.

**Redis `127.0.0.1` not `localhost`**: On Windows, `localhost` resolves to IPv6 `::1` by default; Memurai/Redis only binds to IPv4 `127.0.0.1`, so the explicit IP is required.

---

## AI Tools Used

GitHub Copilot and Claude were used for boilerplate acceleration (entity stubs, JPQL query syntax, Spring Security configuration reference). All business logic (overtime tiers, monthly cap, event-driven SMS pattern, Hikari tuning rationale) was authored and verified manually.

---

