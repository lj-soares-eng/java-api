<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- Modified principles: Technology Constraints (schema management), Development Workflow
- Added: Local dev ddl-auto=update; prod profile validate; MySQL database manual CREATE DATABASE
- Removed sections: none
- Follow-up: application-prod.properties added; README and quickstart.md updated
-->

# Java Users REST API Constitution

## Core Principles

### I. Layered Architecture (NON-NEGOTIABLE)

The application MUST follow a strict layered structure:

- **Controller** — HTTP mapping, request/response handling, status codes only; no business rules.
- **Service** — business logic, validation orchestration, password hashing, transaction boundaries.
- **Repository** — persistence via Spring Data JPA; no HTTP concerns.

Dependencies MUST flow inward (controller → service → repository). Cross-layer shortcuts (e.g., controller calling repository directly) are forbidden unless explicitly justified and documented in the implementation plan.

### II. REST API Conventions

- Resources MUST use plural nouns and consistent paths (`/api/users`, `/api/users/{id}`).
- HTTP verbs MUST match semantics: GET (read), POST (create), PUT (update), DELETE (remove).
- Success and error responses MUST use appropriate status codes (200, 201, 204, 400, 404, 409, 500).
- Request and response bodies MUST use separate DTOs where shape differs from the persistence entity.

### III. Input Validation

- All incoming request DTOs MUST use Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Email`, etc.).
- Invalid input MUST return 400 with a clear, consistent error structure.
- Email uniqueness MUST be enforced at the service or database level with 409 on conflict.

### IV. Password Handling (NON-NEGOTIABLE)

Store BCrypt-hashed passwords. GET `/api/users` and GET `/api/users/{id}` MAY include a field `passwordHash` in JSON for debugging/demo purposes. POST/PUT responses MUST NOT echo plain text password.

Implementation rules:

- MUST hash passwords with `BCryptPasswordEncoder` (or equivalent) before persisting.
- Request DTOs MAY accept plain `password` on POST and PUT only.
- GET list and GET by id responses MAY include `passwordHash` (the stored bcrypt string).
- POST and PUT success responses MUST NOT include plain-text `password`; they MAY include `passwordHash` when consistent with GET responses.
- The persistence layer MUST never store or log plain-text passwords.

### V. Testing Discipline

- Service-layer unit tests MUST cover business rules (validation, hashing, uniqueness).
- Integration tests MUST verify REST endpoints (status codes, JSON shape, password rules).
- Tests MUST assert that plain `password` never appears in GET/POST/PUT response bodies.

## Technology Constraints

- **Language**: Java 17 or newer.
- **Framework**: Spring Boot 3.x (Web, Data JPA, Validation).
- **Database**: MySQL 8+; schema aligned with the `users` table (id, name, email, password, role, created_at).
- **Build**: Maven or Gradle; prefer the project wrapper when present.
- **Configuration**: externalize datasource settings in `application.properties` or `application.yml`; secrets MUST NOT be committed.

### Schema management (NON-NEGOTIABLE)

- **Local development** MUST use `spring.jpa.hibernate.ddl-auto=update` so JPA creates/updates tables from entities.
- **Production profile** MUST use `spring.jpa.hibernate.ddl-auto=validate` (e.g. `application-prod.properties` with `spring.profiles.active=prod`).
- The MySQL **database** `users_api` MUST exist before startup (`CREATE DATABASE users_api` once). Hibernate does **not** create the database—only tables inside an existing database.

## Development Workflow

1. Spec Kit workflow: constitution → specify → clarify (recommended) → plan → tasks → implement.
2. Every feature MUST pass a **Constitution Check** during planning before implementation proceeds.
3. Code changes MUST keep DTOs, entities, and API contracts in sync with `specs/` artifacts.
4. README and API documentation MUST reflect actual response fields (including `passwordHash` policy).

## Governance

This constitution supersedes ad-hoc implementation choices. Amendments require:

1. Updating `.specify/memory/constitution.md` with a version bump and amended date.
2. Recording rationale in the Sync Impact Report comment at the top of this file.
3. Re-running **Constitution Check** on active specs and plans before further implementation.

All pull requests and agent-generated code MUST be reviewed for compliance with Core Principles, especially layered architecture and password handling.

**Version**: 1.1.0 | **Ratified**: 2026-05-16 | **Last Amended**: 2026-05-16
