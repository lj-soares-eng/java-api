# Quickstart: Users REST API

**Feature**: 001-users-crud | **Branch**: `001-users-crud`

## Prerequisites

- JDK 17+
- Maven 3.8+
- MySQL 8 running locally

## 1. Database setup

Create the **database** once (Hibernate does not create databases):

```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS users_api;
```

**Local dev:** `application.properties` uses `spring.jpa.hibernate.ddl-auto=update` — Hibernate creates/updates the `users` table from the `User` entity. No manual `CREATE TABLE` required.

**Production:** use profile `prod` (`application-prod.properties` sets `ddl-auto=validate`). The schema must exist and match entities before startup.

## 2. Application configuration

Set credentials (example):

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

Production run:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

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
