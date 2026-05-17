# Research: Users REST API CRUD

**Feature**: 001-users-crud | **Date**: 2026-05-16

## R1: Spring Boot 3 + Java 17 baseline

- **Decision**: Spring Boot 3.3.x (latest 3.x stable), Java 17, Maven.
- **Rationale**: Matches constitution; Boot 3 requires Java 17+; Maven already on developer machine.
- **Alternatives considered**: Gradle (valid; user specified Maven); Spring Boot 2.x (rejected — EOL alignment with Java 17).

## R2: Password hashing without full Spring Security

- **Decision**: `spring-security-crypto` + `@Bean BCryptPasswordEncoder` only; no `spring-boot-starter-security` filter chain in v1.
- **Rationale**: Constitution requires BCrypt; spec has no authentication endpoints; avoids default security blocking all routes.
- **Alternatives considered**: Full Spring Security (rejected for v1 scope); manual BCrypt library (rejected — encoder is standard).

## R3: JPA schema strategy

- **Decision**: Entity `User` maps to table `users`; unique index on `email`; `ddl-auto=validate` with SQL script in quickstart for manual schema.
- **Rationale**: Aligns with README schema; validate catches drift in demo/prod-like runs.
- **Alternatives considered**: `ddl-auto=update` only (acceptable for first-run local dev — document in quickstart).

## R4: Email uniqueness and 409

- **Decision**: `@Column(unique = true)` on email + service-level `existsByEmail` check before save; map duplicate key to 409 in `GlobalExceptionHandler`.
- **Rationale**: FR-004; clear client feedback.
- **Alternatives considered**: DB-only constraint without handler (rejected — opaque 500).

## R5: PUT update semantics

- **Decision**: Full replacement of name, email, role; `password` optional — if null/blank omitted, keep existing hash.
- **Rationale**: Matches spec assumptions and edge cases.
- **Alternatives considered**: PATCH partial (out of scope for v1).

## R6: passwordHash in JSON

- **Decision**: `UserResponse` always includes `passwordHash` on GET/POST/PUT success for consistency.
- **Rationale**: Constitution allows GET; POST/PUT MAY include when consistent with GET.
- **Alternatives considered**: GET-only passwordHash (rejected per user preference for consistent responses).

## R7: Role validation

- **Decision**: Java `enum Role { USER, ADMIN }` with JPA `@Enumerated(STRING)`.
- **Rationale**: FR-009; type-safe validation.
- **Alternatives considered**: Free-form string (rejected — spec defines allowed values).
