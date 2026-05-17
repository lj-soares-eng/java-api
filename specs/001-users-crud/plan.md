# Implementation Plan: Users REST API CRUD

**Branch**: `001-users-crud` | **Date**: 2026-05-16 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-users-crud/spec.md`

## Summary

Build a Spring Boot 3 REST API for full CRUD on `users` backed by MySQL 8, with layered architecture (controller → service → repository), Jakarta Validation, and BCrypt password hashing per constitution. GET responses expose `passwordHash`; POST/PUT never return plain `password`.

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.x (spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation), spring-security-crypto (BCrypt only), mysql-connector-j, spring-boot-starter-test

**Storage**: MySQL 8, database `users_api`, table `users`

**Testing**: JUnit 5, Spring Boot Test, MockMvc (integration), Mockito (unit)

**Target Platform**: Linux/local JVM server (port 8080 default)

**Project Type**: Single web-service (Maven monolith)

**Performance Goals**: Standard demo/portfolio scale (<100 concurrent clients, sub-second CRUD)

**Constraints**: Layered architecture; no Spring Security filter chain in v1; secrets via env/properties; local `ddl-auto=update`; prod profile `ddl-auto=validate`; MySQL database `users_api` created manually once

**Scale/Scope**: Single `User` entity, 5 REST endpoints, no auth in v1

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Design compliance |
|-----------|--------|-------------------|
| I. Layered Architecture | PASS | `UserController` → `UserService` → `UserRepository`; DTOs at boundary |
| II. REST Conventions | PASS | `/api/users`, correct verbs; 200/201/204/400/404/409 |
| III. Input Validation | PASS | `@Valid` on request DTOs; `GlobalExceptionHandler` for 400/409 |
| IV. Password Handling | PASS | `BCryptPasswordEncoder` in service; `UserResponse.passwordHash`; no plain password in responses |
| V. Testing Discipline | PASS | Unit tests for service; MockMvc integration for password rules |

**Post-design re-check**: PASS — no violations. Complexity Tracking table empty.

## Project Structure

### Documentation (this feature)

```text
specs/001-users-crud/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md             # Created by /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml
src/main/java/com/example/usersapi/
├── UsersApiApplication.java
├── config/
│   └── PasswordEncoderConfig.java
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
├── model/
│   └── User.java
├── dto/
│   ├── UserCreateRequest.java
│   ├── UserUpdateRequest.java
│   └── UserResponse.java
└── exception/
    ├── DuplicateEmailException.java
    ├── UserNotFoundException.java
    └── GlobalExceptionHandler.java
src/main/resources/
└── application.properties
src/test/java/com/example/usersapi/
├── service/
│   └── UserServiceTest.java
└── controller/
    └── UserControllerIntegrationTest.java
```

**Structure Decision**: Single Maven Spring Boot project at repo root. Package base `com.example.usersapi`. Entity column `password` stores BCrypt hash; JSON field `passwordHash` maps from entity in `UserResponse`.

## Implementation Notes

### DTO mapping

| DTO | Fields | Usage |
|-----|--------|--------|
| `UserCreateRequest` | name, email, password, role | POST body |
| `UserUpdateRequest` | name, email, password (optional), role | PUT body |
| `UserResponse` | id, name, email, role, createdAt, passwordHash | All success responses |

### HTTP semantics

| Method | Path | Success | Body |
|--------|------|---------|------|
| GET | `/api/users` | 200 | `UserResponse[]` |
| GET | `/api/users/{id}` | 200 / 404 | `UserResponse` |
| POST | `/api/users` | 201 | `UserResponse` |
| PUT | `/api/users/{id}` | 200 / 404 / 409 | `UserResponse` |
| DELETE | `/api/users/{id}` | 204 / 404 | empty |

### Service responsibilities

- Hash password on create; re-hash on update when `password` present
- Enforce email uniqueness (catch `DataIntegrityViolationException` or pre-check → 409)
- Set `createdAt` via `@PrePersist` or service on create
- Map entity → `UserResponse` including `passwordHash` from stored hash

### Configuration (`application.properties`)

```properties
spring.application.name=users-api
spring.datasource.url=jdbc:mysql://localhost:3306/users_api
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Production (`application-prod.properties`, profile `prod`):

```properties
spring.jpa.hibernate.ddl-auto=validate
```

MySQL database `users_api` MUST be created once (`CREATE DATABASE`); Hibernate does not create the database.
```

### Error response shape

```json
{
  "timestamp": "2026-05-16T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users"
}
```

## Complexity Tracking

> No constitution violations requiring justification.
