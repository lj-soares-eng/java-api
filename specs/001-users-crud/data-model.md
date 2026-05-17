# Data Model: Users REST API

**Feature**: 001-users-crud | **Date**: 2026-05-16

## Entity: User

| Field (Java) | Column (DB) | Type | Constraints | Notes |
|--------------|-------------|------|-------------|-------|
| id | id | BIGINT / INT | PK, AUTO_INCREMENT | Never client-assigned |
| name | name | VARCHAR(255) | NOT NULL | Trimmed |
| email | email | VARCHAR(255) | NOT NULL, UNIQUE | Case-sensitive uniqueness |
| password | password | VARCHAR(255) | NOT NULL | Stores BCrypt hash only |
| role | role | VARCHAR(50) | NOT NULL | Enum: USER, ADMIN |
| createdAt | created_at | TIMESTAMP | NOT NULL | Set on insert only |

## Validation Rules

| Field | Create | Update |
|-------|--------|--------|
| name | Required, not blank | Required, not blank |
| email | Required, valid email | Required, valid email |
| password | Required, min length 8 (recommended) | Optional; if present, re-hash |
| role | Required, USER or ADMIN | Required, USER or ADMIN |
| createdAt | Ignored if sent | Immutable |

## Relationships

None (single-table feature).

## State Transitions

```text
[non-existent] --POST--> [persisted]
[persisted] --PUT--> [persisted] (updated fields)
[persisted] --DELETE--> [removed]
```

## API vs persistence mapping

| API (JSON) | Persistence | Direction |
|------------|---------------|-----------|
| password | — | Request only (create/update) |
| passwordHash | password (column) | Response only |
| createdAt | created_at | Response only |

## DDL (MySQL 8)

```sql
CREATE DATABASE IF NOT EXISTS users_api;
USE users_api;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
