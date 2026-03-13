# Platform Backend (Spring Boot modular monolith → microservices)

Production-oriented backend scaffold for a React client, implemented as a **modular monolith** with clear domain boundaries so each module can later be extracted into its own microservice.

## Tech stack
- Java 17, Spring Boot, Maven
- Spring Web, Spring Security, Spring Data JPA (Hibernate)
- JWT access tokens + DB-backed refresh tokens (hashed, rotated)
- Oracle DB + Flyway migrations
- Docker + Docker Compose + NGINX reverse proxy
- JUnit 5 + Testcontainers (Oracle)
- Lombok + MapStruct

## Module boundaries (DDD-style)
All domain modules live under `src/main/java/com/example/platform/modules`.

- `modules/users`: user management (controller/service/repository/entity/dto/mapper)
- `modules/auth`: authentication + token issuance/refresh (controller/service/repository/entity/dto)

**Rule:** modules may depend on other modules **only via service interfaces** (no cross-module repository/entity access). Example: auth depends on the users module via `UsersService`.

Cross-cutting code:
- `com.example.platform.security`: JWT provider + filter + user-details
- `com.example.platform.config`: global config (security)
- `com.example.platform.common`: shared error model + JPA auditing

## API
### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`

### Users (protected)
- `GET /api/users` (admin-only)
- `GET /api/users/{id}` (self or admin)
- `POST /api/users` (admin-only)
- `PUT /api/users/{id}` (self or admin)
- `DELETE /api/users/{id}` (admin-only)

## Configuration
Runtime config is env-driven via `src/main/resources/application.yml`.

Example env file: `env.example` (copy to your preferred dotenv mechanism).

Variables:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION` (seconds)
- `JWT_REFRESH_EXPIRATION` (seconds)

## Run locally (Docker Compose)
Bring up Oracle + backend + NGINX reverse proxy:

```bash
docker compose up --build
```

Then call the API via NGINX on `http://localhost/api/...`.

## Database migrations
Flyway scripts:
- `src/main/resources/db/migration/V1__create_users_table.sql`
- `src/main/resources/db/migration/V2__create_refresh_tokens_table.sql`

## Integration tests (Testcontainers)
Tests run Spring Boot against a real Oracle container:

```bash
mvn test
```

Key tests:
- `AuthIntegrationTest`: register/login/refresh rotation + use access token on protected endpoint
- `UsersCrudIntegrationTest`: admin vs user authorization behavior

## Extraction path to microservices
Each module already has:
- its own controller/service/repository/entity/dto/mapper packages
- a **service interface** acting as the module boundary

To extract a module into a microservice later:
- copy the module package + its Flyway migrations
- replace intra-module interface calls with REST/gRPC messaging
- keep shared contracts in a dedicated “shared-api” library or OpenAPI schema

