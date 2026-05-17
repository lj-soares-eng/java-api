# Quickstart: Users REST API

**Feature**: 001-users-crud | **Branch**: `001-users-crud`

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8 running locally

## 1. Database setup

```bash
mysql -u root -p
```

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

## 2. Application configuration

Set credentials (example):

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

Or edit `src/main/resources/application.properties` after implementation.

For first local run only, you may use `spring.jpa.hibernate.ddl-auto=update` until the schema exists, then switch to `validate`.

## 3. Run the API

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8080`

## 4. Smoke test (curl)

**Create user**

```bash
curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "securepass123",
    "role": "USER"
  }' | jq .
```

Expect: `201`, body includes `id`, `passwordHash`, no `password` field.

**List users**

```bash
curl -s http://localhost:8080/api/users | jq .
```

**Get by id**

```bash
curl -s http://localhost:8080/api/users/1 | jq .
```

**Update**

```bash
curl -s -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Updated",
    "email": "jane@example.com",
    "role": "ADMIN"
  }' | jq .
```

**Delete**

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X DELETE http://localhost:8080/api/users/1
```

Expect: `204`

## 5. Run tests

```bash
./mvnw test
```

## Contract reference

OpenAPI: [contracts/openapi.yaml](./contracts/openapi.yaml)
