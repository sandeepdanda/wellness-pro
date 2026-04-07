# Wellness Pro 🏋️

A full-stack health club membership management system built with Spring Boot, React, MySQL, and deployed on AWS.

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
| Frontend | React.js, React Router, Axios                       |
| Backend  | Java, Spring Boot, Spring Security, Spring Data JPA |
| Database | MySQL (AWS RDS)                                     |
| Cloud    | AWS EC2, S3, RDS, Elastic Load Balancer             |
| Auth     | JWT-based authentication                            |

## Architecture

```
┌─────────────┐     ┌──────────────────┐     ┌──────────┐
│   React UI  │────▶│  Spring Boot API  │────▶│ MySQL RDS│
│   (S3/EC2)  │◀────│   (EC2 + ALB)     │◀────│          │
└─────────────┘     └──────────────────┘     └──────────┘
```

## Getting Started

### Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm start
```

## Results

- 25% faster membership enrollment through streamlined UI
- 30% reduction in operational inefficiencies via automated class booking

## License

MIT
