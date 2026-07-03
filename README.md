# Java Users REST API

A scalable RESTful Web API built with **Java** and **Spring Boot**, using **Spring Data JPA** for persistence and **MySQL** as the relational database.

This project demonstrates core engineering practices layered architecture, REST conventions, validation, and clean persistence boundaries applied in the Java/Spring ecosystem.

## Features

- RESTful CRUD operations for **users**
- Persistence with **Spring Data JPA** (Hibernate)
- **MySQL** relational database
- Automatic `CreatedAt` timestamp on user creation
- Role-based user model (`Role` field)

## Tech stack

| Layer        | Technology              |
|-------------|-------------------------|
| Language    | Java 17+                |
| Framework   | Spring Boot 3.x         |
| Persistence | Spring Data JPA         |
| Database    | MySQL 8+                |
| Build       | Maven (or Gradle)       |

## Data model

**Table:** `users`

| Column      | Type           | Notes                          |
|------------|----------------|--------------------------------|
| `id`       | INT, PK, AI    | Auto-increment primary key     |
| `name`     | VARCHAR        | User display name              |
| `email`    | VARCHAR        | Unique email                   |
| `password` | VARCHAR        | BCrypt hash (exposed as `passwordHash` in JSON) |
| `role`     | VARCHAR        | e.g. `USER`, `ADMIN`           |
| `created_at` | TIMESTAMP    | Set at creation time           |

## Prerequisites

- **JDK 17+**
- **Maven** or **Gradle**
- **MySQL Server** (running locally or via Docker)
- *(Optional)* MySQL client or GUI (DBeaver, Workbench) for SQL/debugging

## Getting started

### 1. Clone the repository

```bash
git clone https://github.com/lj-soares-eng/java-api.git
cd java-api
```

### 2. Create the database (once)

Hibernate **does not** create the MySQL database—only tables inside it. Run once:

```sql
CREATE DATABASE IF NOT EXISTS `JavaApi`;
```

On local dev, **`spring.jpa.hibernate.ddl-auto=update`** (default in `application.properties`) creates/updates the `users` table from the JPA entity. You do **not** need to run `CREATE TABLE` manually for development.

For **production**, use the `prod` profile (`ddl-auto=validate`) and apply schema via SQL or migrations—the table must already match the entity.

### 3. Configure the application

Set credentials if needed:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

Default datasource settings live in `src/main/resources/application.properties`. Production overrides: `application-prod.properties` — run with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 4. Run the API

```bash
./mvnw spring-boot:run
```

Or with system Maven:

```bash
mvn spring-boot:run
```

The API runs at `http://localhost:8080` by default.


## API endpoints

| Method | Endpoint           | Description      |
|--------|--------------------|------------------|
| GET    | `/api/users`       | List all users   |
| GET    | `/api/users/{id}`  | Get user by ID   |
| POST   | `/api/users`       | Create a user    |
| PUT    | `/api/users/{id}`  | Update a user    |
| DELETE | `/api/users/{id}`  | Delete a user    |

## Example: create user
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "your-secure-password",
    "role": "USER"
  }'
```
Responses include `passwordHash` (BCrypt) on success; plain `password` is never returned. Send `password` only in POST/PUT request bodies.

## Project structure

```text
src/main/java/com/example/usersapi/
├── UsersApiApplication.java
├── controller/     # REST controllers
├── model/          # JPA entities
├── repository/     # Spring Data repositories
├── service/        # Business logic
└── dto/            # Request/response DTOs
```

## Security notes

- Hash passwords with BCrypt before persisting.
- GET `/api/users` and GET `/api/users/{id}` may include `passwordHash` in JSON (demo/debug).
- POST/PUT success responses must not echo plain-text `password` (may include `passwordHash`).
- Validate input (`@Valid`, Bean Validation).
- Use HTTPS in production.

## License
MIT

## Author
Lucas Soares — GitHub
