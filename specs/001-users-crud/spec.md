# Feature Specification: Users REST API CRUD

**Feature Branch**: `001-users-crud`

**Created**: 2026-05-16

**Status**: Draft

**Input**: User description: "Users REST API: CRUD for users (id, name, unique email, password, role, createdAt on create). BCrypt hashing; passwordHash on GET /api/users and GET /api/users/{id}; no plain password on POST/PUT responses."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Create a user (Priority: P1)

An API client registers a new user by submitting name, email, password, and role. The system stores the password in hashed form, assigns an identifier and creation timestamp, and returns the created user without exposing the plain-text password.

**Why this priority**: Creating users is the foundation for all other operations; without it, the API delivers no value.

**Independent Test**: Send a valid create request and verify 201 response with id, profile fields, createdAt, and no plain password in the body.

**Acceptance Scenarios**:

1. **Given** no user exists with the submitted email, **When** the client POSTs valid user data to `/api/users`, **Then** the system returns 201 with id, name, email, role, createdAt, and MUST NOT include plain-text `password`.
2. **Given** valid create data, **When** the user is created, **Then** the stored password is a one-way hash (not plain text).
3. **Given** a duplicate email, **When** the client POSTs to `/api/users`, **Then** the system returns 409 and does not create a second user.
4. **Given** missing or invalid required fields, **When** the client POSTs to `/api/users`, **Then** the system returns 400 with a clear error.

---

### User Story 2 - List and retrieve users (Priority: P2)

An API client lists all users or fetches one user by id to inspect profile data. Responses may include a `passwordHash` field for debugging/demo; plain passwords MUST never appear.

**Why this priority**: Read operations are required to verify creates and support admin or integration workflows.

**Independent Test**: Create users, then GET collection and GET by id; assert JSON shape and password rules.

**Acceptance Scenarios**:

1. **Given** users exist, **When** the client GETs `/api/users`, **Then** the system returns 200 with a list of users including id, name, email, role, createdAt; each item MAY include `passwordHash` and MUST NOT include plain-text `password`.
2. **Given** a user exists with id `{id}`, **When** the client GETs `/api/users/{id}`, **Then** the system returns 200 with that user's fields; response MAY include `passwordHash` and MUST NOT include plain-text `password`.
3. **Given** no user with id `{id}`, **When** the client GETs `/api/users/{id}`, **Then** the system returns 404.

---

### User Story 3 - Update a user (Priority: P3)

An API client updates an existing user's name, email, password, and/or role. Plain password is accepted only on input; success responses MUST NOT echo plain-text password.

**Why this priority**: Updates complete the lifecycle after create and read are available.

**Independent Test**: Create a user, PUT changes, verify persistence and response rules.

**Acceptance Scenarios**:

1. **Given** a user exists, **When** the client PUTs valid changes to `/api/users/{id}`, **Then** the system returns 200 (or 204 per API convention) and updated fields are persisted; response MUST NOT include plain-text `password`.
2. **Given** a new password is sent on update, **When** the update succeeds, **Then** the stored value is re-hashed; plain password MUST NOT appear in the response.
3. **Given** update would duplicate another user's email, **When** the client PUTs, **Then** the system returns 409.
4. **Given** invalid id, **When** the client PUTs `/api/users/{id}`, **Then** the system returns 404.

---

### User Story 4 - Delete a user (Priority: P4)

An API client removes a user by id.

**Why this priority**: Deletion completes CRUD but depends on users existing first.

**Independent Test**: Create a user, DELETE by id, confirm subsequent GET returns 404.

**Acceptance Scenarios**:

1. **Given** a user exists, **When** the client DELETEs `/api/users/{id}`, **Then** the system returns 204 (or 200) and the user is no longer retrievable.
2. **Given** no user with id `{id}`, **When** the client DELETEs `/api/users/{id}`, **Then** the system returns 404.

---

### Edge Cases

- Empty or whitespace-only name or email → 400.
- Invalid email format → 400.
- Password omitted on create → 400 (password required on create).
- Password omitted on update → existing hash unchanged (partial update semantics).
- Role value not in allowed set → 400 (assumed allowed: `USER`, `ADMIN`).
- `createdAt` MUST be set by the system on create; clients MUST NOT supply it on create.
- `createdAt` MUST NOT be modifiable via update.
- Concurrent duplicate email on create/update → one succeeds, other receives 409.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST expose CRUD operations for users at `/api/users` and `/api/users/{id}`.
- **FR-002**: Each user MUST have: unique identifier, name, unique email, hashed password, role, and createdAt timestamp.
- **FR-003**: System MUST set createdAt automatically at creation time; MUST NOT accept client-provided createdAt on create.
- **FR-004**: System MUST enforce unique email across all users (409 on conflict).
- **FR-005**: System MUST validate required fields and formats (name, email, password on create, role) and return 400 on failure.
- **FR-006**: System MUST hash passwords with a one-way algorithm before persistence; plain passwords MUST NOT be stored.
- **FR-007**: GET `/api/users` and GET `/api/users/{id}` MAY include `passwordHash` in JSON; MUST NOT include plain-text `password`.
- **FR-008**: POST and PUT success responses MUST NOT include plain-text `password`; they MAY include `passwordHash` when consistent with GET responses.
- **FR-009**: System MUST support roles as discrete values (minimum: `USER`, `ADMIN`).
- **FR-010**: System MUST return 404 when referencing a non-existent user id.

### Key Entities

- **User**: Represents a registered identity in the system.
  - **id**: Unique identifier (system-generated).
  - **name**: Display name.
  - **email**: Unique contact/login identifier.
  - **password**: Plain text only on incoming create/update requests; never in responses.
  - **passwordHash**: Stored one-way hash; optional in GET responses for demo/debug.
  - **role**: Access or classification label (`USER`, `ADMIN`).
  - **createdAt**: Timestamp when the user was created (system-assigned, immutable).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A client can complete create → list → get → update → delete for a single user without manual database intervention.
- **SC-002**: 100% of successful GET list/detail responses in acceptance testing contain zero plain-text `password` fields.
- **SC-003**: 100% of successful POST/PUT responses in acceptance testing contain zero plain-text `password` fields.
- **SC-004**: Duplicate email attempts are rejected with a conflict outcome (409) in 100% of tested cases.
- **SC-005**: Invalid payloads are rejected with 400 in 100% of tested validation scenarios.

## Assumptions

- No authentication or authorization layer in v1; any client with API access may perform CRUD (suitable for local demo/portfolio).
- Role is a string enum with values `USER` and `ADMIN`; no role-based endpoint restrictions in v1.
- Partial update on PUT: omitted password leaves existing hash unchanged; other fields follow full-replacement semantics unless clarified in planning.
- API is JSON over HTTP; error bodies use a consistent structure (details defined in implementation plan).
- Password hashing algorithm and exact response DTO mapping align with project constitution (BCrypt, layered architecture).
