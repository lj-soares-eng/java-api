# Tasks: Users REST API CRUD

**Input**: Design documents from `/specs/001-users-crud/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Included per constitution (service unit tests + MockMvc integration tests).

**Organization**: Tasks grouped by user story for independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: User story label (US1–US4)

## Path Conventions

- Single Maven project at repository root
- Source: `src/main/java/com/example/usersapi/`
- Tests: `src/test/java/com/example/usersapi/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize Spring Boot Maven project

- [ ] T001 Create `pom.xml` with Spring Boot 3 parent, web, data-jpa, validation, mysql-connector-j, spring-security-crypto, spring-boot-starter-test
- [ ] T002 Create `src/main/java/com/example/usersapi/UsersApiApplication.java` main class
- [ ] T003 [P] Create `src/main/resources/application.properties` per plan.md (datasource, JPA, env vars)
- [ ] T004 [P] Add Maven Wrapper (`mvnw`, `.mvn/wrapper/`) for reproducible builds

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain, persistence, security config, and error handling — blocks all user stories

**⚠️ CRITICAL**: No user story endpoint work until this phase is complete

- [ ] T005 [P] Create `src/main/java/com/example/usersapi/model/Role.java` enum (USER, ADMIN)
- [ ] T006 Create `src/main/java/com/example/usersapi/model/User.java` JPA entity per data-model.md
- [ ] T007 Create `src/main/java/com/example/usersapi/repository/UserRepository.java` with `existsByEmail` and standard JpaRepository methods
- [ ] T008 [P] Create `src/main/java/com/example/usersapi/dto/UserCreateRequest.java` with Jakarta Validation annotations
- [ ] T009 [P] Create `src/main/java/com/example/usersapi/dto/UserUpdateRequest.java` with optional password field
- [ ] T010 [P] Create `src/main/java/com/example/usersapi/dto/UserResponse.java` with passwordHash field (no password field)
- [ ] T011 Create `src/main/java/com/example/usersapi/config/PasswordEncoderConfig.java` with BCryptPasswordEncoder bean
- [ ] T012 [P] Create `src/main/java/com/example/usersapi/exception/UserNotFoundException.java`
- [ ] T013 [P] Create `src/main/java/com/example/usersapi/exception/DuplicateEmailException.java`
- [ ] T014 Create `src/main/java/com/example/usersapi/exception/GlobalExceptionHandler.java` (400, 404, 409, 500) per plan.md error shape
- [ ] T015 Create `src/main/java/com/example/usersapi/service/UserService.java` skeleton with entity↔UserResponse mapper including passwordHash

**Checkpoint**: Foundation ready — user story endpoints can be implemented

---

## Phase 3: User Story 1 - Create a user (Priority: P1) 🎯 MVP

**Goal**: POST `/api/users` creates a user with BCrypt-hashed password; 201 response without plain password

**Independent Test**: `curl -X POST /api/users` returns 201 with id, fields, passwordHash; duplicate email → 409; invalid body → 400

### Tests for User Story 1

- [ ] T016 [P] [US1] Create `src/test/java/com/example/usersapi/service/UserServiceTest.java` — test create hashes password and maps passwordHash
- [ ] T017 [P] [US1] Create `src/test/java/com/example/usersapi/controller/UserControllerIntegrationTest.java` — POST success, 400 validation, 409 duplicate (use @SpringBootTest + MockMvc + test DB or @DataJpaTest slice as appropriate)

### Implementation for User Story 1

- [ ] T018 [US1] Implement `createUser` in `src/main/java/com/example/usersapi/service/UserService.java` (hash password, set createdAt, uniqueness check)
- [ ] T019 [US1] Implement POST handler in `src/main/java/com/example/usersapi/controller/UserController.java` at `/api/users` returning 201 + UserResponse

**Checkpoint**: User Story 1 independently testable via POST only

---

## Phase 4: User Story 2 - List and retrieve users (Priority: P2)

**Goal**: GET `/api/users` and GET `/api/users/{id}` return users with passwordHash; never plain password

**Independent Test**: After creating users, GET list and GET by id return 200; unknown id → 404

### Tests for User Story 2

- [ ] T020 [P] [US2] Add list/get tests to `src/test/java/com/example/usersapi/service/UserServiceTest.java`
- [ ] T021 [P] [US2] Add GET integration tests to `src/test/java/com/example/usersapi/controller/UserControllerIntegrationTest.java` (assert no `password` key in JSON)

### Implementation for User Story 2

- [ ] T022 [US2] Implement `findAll` and `findById` in `src/main/java/com/example/usersapi/service/UserService.java`
- [ ] T023 [US2] Add GET `/api/users` and GET `/api/users/{id}` in `src/main/java/com/example/usersapi/controller/UserController.java`

**Checkpoint**: User Stories 1 and 2 work independently

---

## Phase 5: User Story 3 - Update a user (Priority: P3)

**Goal**: PUT `/api/users/{id}` updates fields; optional password re-hash; no plain password in response

**Independent Test**: PUT changes name/role; omit password keeps hash; duplicate email → 409; missing id → 404

### Tests for User Story 3

- [ ] T024 [P] [US3] Add update tests to `src/test/java/com/example/usersapi/service/UserServiceTest.java` (password optional semantics)
- [ ] T025 [P] [US3] Add PUT integration tests to `src/test/java/com/example/usersapi/controller/UserControllerIntegrationTest.java`

### Implementation for User Story 3

- [ ] T026 [US3] Implement `updateUser` in `src/main/java/com/example/usersapi/service/UserService.java`
- [ ] T027 [US3] Add PUT `/api/users/{id}` in `src/main/java/com/example/usersapi/controller/UserController.java`

**Checkpoint**: User Stories 1–3 work independently

---

## Phase 6: User Story 4 - Delete a user (Priority: P4)

**Goal**: DELETE `/api/users/{id}` returns 204; missing id → 404

**Independent Test**: DELETE then GET returns 404

### Tests for User Story 4

- [ ] T028 [P] [US4] Add delete tests to `src/test/java/com/example/usersapi/service/UserServiceTest.java`
- [ ] T029 [P] [US4] Add DELETE integration tests to `src/test/java/com/example/usersapi/controller/UserControllerIntegrationTest.java`

### Implementation for User Story 4

- [ ] T030 [US4] Implement `deleteUser` in `src/main/java/com/example/usersapi/service/UserService.java`
- [ ] T031 [US4] Add DELETE `/api/users/{id}` in `src/main/java/com/example/usersapi/controller/UserController.java`

**Checkpoint**: Full CRUD independently functional

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, validation run, README alignment

- [ ] T032 [P] Update `README.md` Security/API sections for passwordHash on GET and POST/PUT rules
- [ ] T033 Run `./mvnw test` and fix any failures
- [ ] T034 Validate `specs/001-users-crud/quickstart.md` curl flow against running API
- [ ] T035 [P] Add `.gitignore` entries for `target/`, `.env`, IDE files if not present

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)** → **Phase 2 (Foundational)** → **Phases 3–6 (User Stories)** → **Phase 7 (Polish)**
- User stories SHOULD follow priority order P1→P4 (each builds on shared service/controller file)

### User Story Dependencies

| Story | Depends on | Notes |
|-------|------------|-------|
| US1 (Create) | Phase 2 | MVP — first endpoint |
| US2 (Read) | Phase 2, US1 recommended | Needs existing users for meaningful list tests |
| US3 (Update) | Phase 2, US1 | Needs users to update |
| US4 (Delete) | Phase 2, US1 | Needs users to delete |

### Parallel Opportunities

- Phase 1: T003, T004 parallel after T001
- Phase 2: T005–T010, T012–T013 parallel before T015
- Within each story: test tasks T016/T017, T020/T021, etc. marked [P] can run in parallel
- T032 and T035 parallel in Polish phase

### Parallel Example: Phase 2

```bash
# After T006 User entity:
Task T008: dto/UserCreateRequest.java
Task T009: dto/UserUpdateRequest.java
Task T010: dto/UserResponse.java
Task T012: exception/UserNotFoundException.java
Task T013: exception/DuplicateEmailException.java
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 + Phase 2
2. Complete Phase 3 (US1)
3. **STOP and VALIDATE** with quickstart POST curl
4. Demo create-user flow

### Incremental Delivery

1. Setup + Foundational → base ready
2. US1 → POST works (MVP)
3. US2 → GET list/detail
4. US3 → PUT update
5. US4 → DELETE
6. Polish → README + full test suite

---

## Notes

- Total tasks: **35**
- Per story: US1=4 impl+2 test, US2=2+2, US3=2+2, US4=2+2, Setup=4, Foundational=11, Polish=4
- Suggested MVP scope: **T001–T019** (through US1 checkpoint)
- Commit after each phase or logical task group
