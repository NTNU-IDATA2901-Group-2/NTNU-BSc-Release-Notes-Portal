# Release Notes Portal

A web application for viewing, creating, editing, and publishing release notes. The project consists of a **Spring Boot** backend (Java) and a **Vue 3 / TypeScript** frontend (Vite + Tailwind CSS), with **Keycloak** for authentication and **PostgreSQL** as the database.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Running in Development Mode](#running-in-development-mode)
- [Running in Production Mode](#running-in-production-mode)
- [Running in CI Mode](#running-in-ci-mode)

---

## Prerequisites

| Tool | Minimum version |
|------|-----------------|
| Java | 25 |
| Maven | 3.9+ |
| Node.js | 20+ |
| pnpm | 10+ |
| Docker & Docker Compose | latest |

---

## Environment Setup

Copy the provided example environment file and fill in the values:

```bash
cp .env.example .env
```

The `.env` file is used by both Docker Compose and the Spring Boot backend. The default values in `.env.example` work out of the box for local development.

| Variable | Description | Default |
|----------|-------------|---------|
| `KC_USERNAME` | Keycloak admin username | `admin` |
| `KC_PASSWORD` | Keycloak admin password | `admin` |
| `KC_PORT` | Keycloak port | `8081` |
| `KC_REALM` | Keycloak realm name | `my_realm` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `postgres` |
| `DB_DATABASE` | PostgreSQL database name | `db_name` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_HOST` | PostgreSQL host | `localhost` |
| `DB_URL` | Full JDBC URL (auto-composed) | — |

---

## Running in Development Mode

Development mode uses the `dev` Spring profile. The backend automatically starts the Docker Compose services (PostgreSQL + Keycloak) on launch via the `spring-boot-docker-compose` integration, so no manual `docker compose` command is needed.

### Backend

```bash
./mvnw spring-boot:run
```

- The `dev` profile is active by default (see `application.properties`).
- Docker Compose (`docker-compose.yml`) is started automatically, bringing up PostgreSQL and Keycloak.
- The database is seeded with `data.sql` and `data-dev.sql`.
- The API is available at `http://localhost:8080`.

### Frontend

```bash
cd frontend
pnpm install
pnpm run dev
```

The Vite dev server starts at `http://localhost:5173` with hot module replacement (HMR) and proxies API requests to the backend.

---

## Running in Production Mode

In production mode the Spring Boot backend serves the frontend as static content. The frontend must be **compiled and copied into the backend's static resources folder** before packaging, so that a single JAR file contains and serves the entire application.

### Frontend

Build the optimized static files:

```bash
cd frontend
pnpm install
pnpm build
```

Copy the compiled output into the backend's static resources folder so the backend can serve it:

```bash
cp -r frontend/dist/* src/main/resources/static/
```

### Backend

Package and run the application:

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar --spring.profiles.active=prod
```

The application is available at `http://localhost:8080`. The backend serves both the API and the compiled frontend from the same port.

> **Note:** The `prod` profile (`application-prod.properties`) reads database credentials from `.env` and never seeds test data. Make sure your `.env` is configured with the correct production values before starting.

---

## Running in CI Mode

CI mode is designed for automated pipelines. It uses an **in-memory H2 database** (no external services required) and seeds it with `data-ci.sql`.

The full CI pipeline (lint → build → test) is defined in `.github/workflows/build-test-push.yml` and runs automatically on pushes and pull requests to `main` and `dev`.

To replicate the CI steps locally:

### Frontend

Lint and build the frontend:

```bash
cd frontend
pnpm install
pnpm eslint
pnpm build
```

Copy the compiled output into the backend's static resources folder:

```bash
cp -r frontend/dist/* src/main/resources/static/
```

### Backend

Package and start the application with the CI profile:

```bash
./mvnw clean package
java -jar target/*.jar --spring.profiles.active=ci
```

The CI profile (`application-ci.properties`) disables Docker Compose integration and uses an in-memory H2 database, so no external services are needed.

### API Tests

Install [Bruno CLI](https://docs.usebruno.com/bru-cli/overview) if not already installed:

```bash
npm install -g @usebruno/cli
```

Then run the tests:

```bash
cd test/ReleaseNoteAPITests
bru run --env Local
```
