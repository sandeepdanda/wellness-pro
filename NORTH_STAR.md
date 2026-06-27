# Wellness Pro — North Star

Futuristic goals for this project. Current state, vision, and the next moves.
Update as phases ship.

## What it is today

A full-stack health-club membership system, working end-to-end.

- **Backend:** Spring Boot 3.4 / Java 21, layered (controller → service →
  repository), Spring Security + JWT (BCrypt, role-based MEMBER/ADMIN), response
  DTOs (no entity leakage), bean validation + global exception handler. H2 in
  dev, PostgreSQL in prod via profiles. Lombok via explicit annotation processor.
- **Domain:** members, locations, membership plans, fitness classes, bookings,
  payments. Capacity-aware booking (past-class / full / double-book all
  rejected), subscription + simulated payments, admin analytics (revenue,
  occupancy, top classes), admin class management.
- **Frontend:** React 18 + Vite + Tailwind + TanStack Query. 8 pages across
  member and admin flows. Spa/zen visual direction (sage + clay palette).
- **Tests:** 10 unit + 7 integration, all green. Verified end-to-end in browser.

## The vision

Membership management that feels like a wellness app, not gym back-office
software — calm, fast, and honest. A member books a class in two taps; an admin
sees the health of every location at a glance. The kind of full-lifecycle,
role-aware product an SDE owns end to end.

## Next level — roadmap (highest value first)

1. **Auto-renewal + real payment lifecycle.** A scheduled job that renews
   subscriptions, records payments, handles failures/dunning. Today payments are
   simulated and one-shot.
2. **Real payment gateway.** Swap the simulated `SubscriptionService` payment for
   Stripe test mode behind an interface, keeping the simulated impl for dev.
3. **Class scheduling depth.** Recurring classes, waitlists (auto-promote when a
   spot frees), cancellation windows, instructor calendars.
4. **Member engagement.** Attendance streaks, a fitness-tracker-style stats page
   (Strava/Apple Fitness vibe), email/push reminders before booked classes.
5. **Ops hardening.** Docker Compose (Postgres + backend + frontend), a CI
   pipeline (build + test on push), and cloud deploy. All deferred today.

## Constraints that don't change

- No git commit/push — left to the human.
- Don't touch `~/.aws/` or system config. Apply AWS production-safety rules.
- Response DTOs at the API boundary — passwords never leave the server.
- H2 for dev/test, Postgres for prod — keep the profile split.
- Ship one slice at a time, evidence-first, before/after report on localhost.

## Working agreement (how to build here)

Understand the layer seams → service layer first → DTOs at the boundary →
controller → frontend → unit + integration tests → browser-verify → localhost
report. Surgical, match existing idiom, no speculative abstractions.
