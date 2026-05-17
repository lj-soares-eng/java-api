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
git clone https://github.com/YOUR_USERNAME/java-api.git
cd java-api
```

### 2. Create the database

```bash
CREATE DATABASE users_api;
USE users_api;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Configure the application
Update src/main/resources/application.properties (or application.yml):

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/users_api
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

Use ddl-auto=update only during early development if you prefer Hibernate to manage schema changes.

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
MIT (or specify your license here.)

## Author
Lucas Soares — GitHub