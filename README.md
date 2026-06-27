# Wellness Pro 🏋️

A full-stack health club membership management system: Spring Boot + React. Runs locally with zero database setup (in-memory H2), and targets PostgreSQL in production.

## Features

- **Member Management** — Register, update, and track member profiles across multiple gym locations
- **Class Bookings** — Browse and book fitness classes with real-time availability
- **Membership Plans** — Manage subscription tiers (monthly, quarterly, annual) with auto-renewal
- **Admin Dashboard** — View membership analytics, revenue tracking, and occupancy metrics
- **User Dashboard** — Personal activity log, upcoming bookings, and membership status
- **Multi-Location** — Support for multiple gym branches with location-specific schedules

## Tech Stack

| Layer    | Technology                                          |
| -------- | --------------------------------------------------- |
| Frontend | React 18, Vite, Tailwind CSS, TanStack Query, React Router, Axios |
| Backend  | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA |
| Database | H2 (dev/test), PostgreSQL (prod)                    |
| Auth     | JWT (jjwt 0.12), BCrypt, role-based (MEMBER / ADMIN) |

## Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────┐
│   React UI  │────▶│  Spring Boot API  │────▶│ H2 (dev)     │
│   (Vite)    │◀────│  JWT + REST       │◀────│ Postgres(prd)│
└─────────────┘     └──────────────────┘     └──────────────┘
```

Layered backend: `controller` → `service` → `repository`, with DTOs at the API
boundary (entities never leak the password hash). Booking enforces capacity and
prevents double-booking inside a transaction.

## Getting Started

Runs with no database install. The dev profile uses in-memory H2 and seeds demo data.

### Backend

```bash
cd backend
./mvnw spring-boot:run        # starts on :8080, profile "dev"
```

Demo accounts (password `password123`):
- `admin@wellnesspro.dev` — ADMIN (analytics, member list, class management)
- `member@wellnesspro.dev` — MEMBER (browse, book, cancel)

### Frontend

```bash
cd frontend
npm install
npm run dev                   # starts on :5173, proxies /api to :8080
```

### Tests

```bash
cd backend && ./mvnw test     # 7 unit + 4 integration tests
```

### Production (PostgreSQL)

```bash
cd backend
SPRING_PROFILES_ACTIVE=prod \
  DB_URL=jdbc:postgresql://<host>:5432/wellness_pro \
  DB_USERNAME=<user> DB_PASSWORD=<pass> \
  JWT_SECRET=<256-bit-secret> ./mvnw spring-boot:run
```

## API

| Method | Endpoint | Access | Purpose |
| ------ | -------- | ------ | ------- |
| POST | `/api/auth/register` | public | Create account, returns JWT |
| POST | `/api/auth/login` | public | Authenticate, returns JWT |
| GET | `/api/classes` | member | List classes with availability |
| POST | `/api/bookings` | member | Book a class (capacity-checked) |
| PATCH | `/api/bookings/{id}/cancel` | member | Cancel and free the spot |
| GET | `/api/members/me` | member | Own profile |
| GET | `/api/admin/analytics` | admin | Revenue + occupancy metrics |
| POST | `/api/classes` | admin | Create a class |

## License

MIT
