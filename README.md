# Release Notes Portal 

Release Notes Portal is a full-stack application for creating and publishing release notes.

- Backend: Spring Boot 4 (Java 25)
- Frontend: Vue 3 + TypeScript + Vite
- Auth: Keycloak
- Database: PostgreSQL

This project supports three runtime modes:

- dev (local development)
- ci (automated/local CI-like runs)
- prod (containerized deployment behind Caddy)

## Prerequisites

| Tool | Version |
|---|---|
| Java | 25 |
| Maven | 3.9+ |
| Node.js | 20+ |
| pnpm | 10+ |
| Docker + Docker Compose | Recent version |

## Environment Setup

Create your environment file:

```bash
cp .env.example .env
```

Fill all values in `.env` before starting the app.

Main variables used by the backend and compose:

| Variable | Purpose |
|---|---|
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins allowed for `/api/**` |
| `KC_USERNAME` / `KC_PASSWORD` | Keycloak bootstrap admin account |
| `KC_URL` | Keycloak base URL used by frontend/backend config |
| `KC_ISSUER_URL` | JWT issuer URL used by Spring Security |
| `KC_JWK_SET_URI` | JWK endpoint used to validate tokens |
| `KC_CLIENT_ID` | Keycloak client id used by frontend |
| `KC_REALM` | Keycloak realm name |
| `DB_USERNAME` / `DB_PASSWORD` / `DB_DATABASE` | PostgreSQL credentials |
| `DB_URL` | JDBC URL for Spring datasource |
| `OPENAI_URL` / `OPENAI_API_KEY` / `OPENAI_MODEL` | AI integration settings. During development the Groq API was used. This requires signing up to acquire an API key. |
| `CHANGE_NOTE_DIRECTORY` | Relative/child change note directory used by git features |

## Development Mode (dev)

The default Spring profile is `dev`.

### 1) Start backend (main dev command)

```bash
./mvnw spring-boot:run
```

What happens:

- `spring-boot-docker-compose` automatically starts services from `docker-compose.yml`.
- PostgreSQL and Keycloak are started for you.
- `data.sql` and `data-dev.sql` are loaded.
- Backend is available on `http://localhost:8080`.

### 2) Set up Keycloak realm and client

After first startup, configure Keycloak (required in dev):

1. Open `http://localhost:8081` and log in with `KC_USERNAME` / `KC_PASSWORD`.
2. Import the test realm file `dev-realm.json` from the project root.
3. Ensure the imported realm name matches `KC_REALM` (or update env values accordingly).
4. If needed, verify the client matching `KC_CLIENT_ID` exists and is configured for browser login flow (standard flow + PKCE for SPA usage).
5. Verify redirect URI for frontend dev server, for example `http://localhost:5173/*`.
6. Verify web origin for frontend dev server, for example `http://localhost:5173`.
7. Create a user for test purposes and assign roles under the `release-note` client: at minimum `Admin`, and customer roles when required.

### 3) Start frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs on `http://localhost:5173`.

Important note: in dev, the frontend uses `http://localhost:8080/api/` directly (no Vite proxy is configured).

## Production Mode (prod)

Use the provided production compose example, as requested by this project setup.

### 1) Prepare files

```bash
cp docker-compose-prod-example.yml docker-compose-prod.yml
cp Caddyfile-example Caddyfile
```

Then edit `docker-compose-prod.yml` and fill in all empty environment values.

### 2) Prepare local hostnames for reverse proxy

The default Caddy config uses:

- `app.app.local` for the app
- `auth.app.local` for Keycloak

Add them to your hosts file (for local testing):

```text
127.0.0.1 app.app.local
127.0.0.1 auth.app.local
```

### 3) Run production stack

```bash
docker compose -f docker-compose-prod.yml up -d
```

Production notes:

- Caddy is the reverse proxy and uses self-signed certificates via `tls internal`.
- Browser/security tools may warn until trust is configured for the local CA.
- App should be reachable at `https://app.app.local`.
- Keycloak should be reachable at `https://auth.app.local`.

## CI Mode (ci)

CI profile is for automated runs and local CI-like testing.

Behavior:

- Uses in-memory H2 database.
- Disables Docker Compose integration (`spring.docker.compose.enabled=false`).
- Seeds with `data-ci.sql`.
- Uses CI security config that permits requests and supports test role headers.

Run locally:

```bash
cd frontend
pnpm install
pnpm eslint
pnpm build
cd ..
mkdir -p src/main/resources/static
cp -r frontend/dist/* src/main/resources/static/
./mvnw clean package
java -jar target/*.jar --spring.profiles.active=ci
```

Run API tests with Bruno:

```bash
npm install -g @usebruno/cli
cd test/ReleaseNoteAPITests
bru run --env Local --sandbox=developer
```

## Useful Endpoints

- App/API base in dev: `http://localhost:8080`
- Frontend dev server: `http://localhost:5173`
- Public frontend runtime config endpoint (dev/prod): `GET /api/public/config`
- OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`

## Build Artifact Notes

The production Docker image expects a prebuilt Spring Boot JAR in `target/`.

If you package manually for prod, build frontend first and copy `frontend/dist` into `src/main/resources/static` before creating the JAR, so the backend serves the frontend assets.


## Authors

- Kristian Nærum Garder
- Stian Øye Jenssen
- Ludvik Lund-Hole
