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

Development mode starts the backend with hot-reload via Spring Boot DevTools and the frontend with the Vite dev server (HMR).

### 1. Start infrastructure (PostgreSQL + Keycloak)

```bash
docker compose up -d
```

### 2. Start the backend

```bash
./mvnw spring-boot:run
```

The backend defaults to the `dev` profile (see `application.properties`). It connects to PostgreSQL and Keycloak using the values from `.env`, and seeds the database with `data.sql` + `data-dev.sql`.

The API is available at `http://localhost:8080`.

### 3. Start the frontend

```bash
cd frontend
pnpm install
pnpm dev
```

The frontend dev server runs at `http://localhost:5173` and proxies API requests to the backend.

---

## Running in Production Mode

Production mode runs the fully-built frontend as static files served by the Spring Boot backend.

### 1. Build the frontend

```bash
cd frontend
pnpm install
pnpm build
```

This generates optimised static files in `frontend/dist/`.

### 2. Copy the static files into the backend

```bash
cp -r frontend/dist/* src/main/resources/static/
```

### 3. Package the backend

```bash
./mvnw clean package -DskipTests
```

### 4. Start infrastructure

```bash
docker compose up -d
```

### 5. Run the application

```bash
java -jar target/*.jar --spring.profiles.active=prod
```

The application is available at `http://localhost:8080`.

The `prod` profile (`application-prod.properties`) connects to the PostgreSQL database specified in `.env` and never seeds test data.

---

## Running in CI Mode

CI mode is designed for automated pipelines. It uses an **in-memory H2 database** (no external services required) and seeds it with `data-ci.sql`.

The full CI pipeline (lint → build → test) is defined in `.github/workflows/build-test-push.yml` and runs automatically on pushes and pull requests to `main` and `dev`.

To replicate the CI steps locally:

### 1. Build and lint the frontend

```bash
cd frontend
pnpm install
pnpm eslint
pnpm build
```

### 2. Copy the static files and package the backend

```bash
cp -r frontend/dist/* src/main/resources/static/
./mvnw clean package
```

### 3. Start the application with the CI profile

```bash
java -jar target/*.jar --spring.profiles.active=ci
```

The CI profile (`application-ci.properties`) disables Docker Compose integration and uses an in-memory H2 database, so no external services are needed.

### 4. Run the API tests

Install [Bruno CLI](https://docs.usebruno.com/bru-cli/overview) if not already installed:

```bash
npm install -g @usebruno/cli
```

Then run the tests:

```bash
cd test/ReleaseNoteAPITests
bru run --env Local
```
