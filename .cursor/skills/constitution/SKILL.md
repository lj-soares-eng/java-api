---
name: constitution
description: >-
  Updates project constitution with BCrypt password and passwordHash JSON rules
  for the Users REST API. Use when the user invokes /constitution or /contitution,
  or asks to set password handling in the project constitution.
compatibility: Requires spec-kit project structure with .specify/memory/constitution.md
---

# Constitution — Password handling

Apply the following governing rule to `.specify/memory/constitution.md` and ensure specs, plans, and implementation comply.

## Required policy (verbatim)

Store BCrypt-hashed passwords. GET /api/users and GET /api/users/{id} may include a field "passwordHash" in JSON for debugging/demo purposes. POST/PUT responses must not echo plain text password.

## Execution steps

1. Load `.specify/memory/constitution.md`. If missing, copy from `.specify/templates/constitution-template.md` first.

2. Ensure the constitution includes a **Security — Password handling** principle (under Core Principles or a dedicated Security section) containing the **Required policy (verbatim)** text above without paraphrasing.

3. Add implementation constraints the agent must follow:
   - Hash with `BCryptPasswordEncoder` (or equivalent) before persisting.
   - Request DTOs may accept plain `password` on POST/PUT only.
   - Response DTOs for GET list/detail may include `passwordHash`; never include plain `password`.
   - POST/PUT success responses must not echo plain-text `password` (may include `passwordHash` if consistent with GET).

4. Replace remaining `[PLACEHOLDER]` tokens in the constitution where possible using repo context (README, existing specs). Set `LAST_AMENDED_DATE` to today. Bump `CONSTITUTION_VERSION` (MINOR if adding this principle).

5. Report what was updated and remind the user to run `speckit-specify` / `speckit-plan` if specs predate this rule.

## JSON examples

**GET /api/users/{id}** (allowed):

```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "USER",
  "createdAt": "2026-05-16T12:00:00Z",
  "passwordHash": "$2a$10$..."
}
```

**POST /api/users** response (must not include plain password):

```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "role": "USER",
  "createdAt": "2026-05-16T12:00:00Z",
  "passwordHash": "$2a$10$..."
}
```

**POST /api/users** request (plain password on input only):

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "plain-text-only-on-request",
  "role": "USER"
}
```
