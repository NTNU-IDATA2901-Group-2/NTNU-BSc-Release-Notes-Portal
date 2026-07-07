# Release Notes Portal 

Release Notes Portal is a full-stack application for creating and publishing release notes.

- Backend: Spring Boot 4 (Java 25)
- Frontend: Vue 3 + TypeScript + Vite
- Auth: any OIDC-compliant identity provider (dev setup uses Keycloak)
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
| Node.js | 20.19+ |
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
| `KC_USERNAME` / `KC_PASSWORD` | Bootstrap admin account for the dev Keycloak container only |
| `OIDC_ISSUER_URI` | OIDC issuer URL; used for discovery and `iss` validation, and served to the frontend |
| `OIDC_CLIENT_ID` | OIDC client/application id used by the frontend |
| `OIDC_SCOPES` | Scopes the frontend requests (default `openid profile`) |
| `OIDC_AUDIENCE` | Optional expected `aud` of access tokens (empty disables audience validation) |
| `OIDC_ROLES_CLAIM` | Optional claim holding role strings (default `roles`) |
| `OIDC_CUSTOMER_CLAIM` | Optional claim holding customer entries (default `groups`) |
| `OIDC_CUSTOMER_PREFIX` | Optional prefix marking customer entries (default `/Customers/`) |
| `DB_USERNAME` / `DB_PASSWORD` / `DB_DATABASE` | PostgreSQL credentials |
| `DB_URL` | JDBC URL for Spring datasource |
| `OPENAI_URL` / `OPENAI_API_KEY` / `OPENAI_CHAT_COMPLETIONS_PATH` / `OPENAI_MODEL` | AI integration settings for any OpenAI-compatible API. `OPENAI_CHAT_COMPLETIONS_PATH` sets the chat completions endpoint path. |
| `JIRA_BASE_URL` / `JIRA_EMAIL` / `JIRA_API_TOKEN` | Jira integration base URL and API credentials |
| `RELEASE_NOTE_DIRECTORY` | Relative release note directory used by git features. The change note directory and personal access token are configured per git repository in the portal |

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
3. Ensure the imported realm name matches the realm in `OIDC_ISSUER_URI` (or update env values accordingly).
4. If needed, verify the client matching `OIDC_CLIENT_ID` exists and is configured for browser login flow (standard flow + PKCE for SPA usage).
5. Verify redirect URI for frontend dev server, for example `http://localhost:5173/*`.
6. Verify web origin for frontend dev server, for example `http://localhost:5173`.
7. Create a user for test purposes and assign the `Admin` role under the `release-note` client. For customer access, add the user to a subgroup of `/Customers` (e.g. `/Customers/ACME`) instead of assigning a role.

### 3) Start frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs on `http://localhost:5173`.

Important note: in dev, the frontend uses `http://localhost:8080/api/` directly (no Vite proxy is configured). The dev IdP settings default to the local Keycloak realm and can be overridden with `VITE_OIDC_AUTHORITY`, `VITE_OIDC_CLIENT_ID`, `VITE_OIDC_SCOPES` and `VITE_OIDC_ROLES_CLAIM`.

## Identity Provider Configuration

The app is IdP-agnostic: it works with any OIDC-compliant identity provider, selected purely through environment variables. The backend validates JWTs via OIDC discovery on `OIDC_ISSUER_URI` and maps token claims to authorities using a configurable contract:

- Entries in `OIDC_ROLES_CLAIM` become roles (`Admin` → `ROLE_ADMIN`).
- Entries in `OIDC_CUSTOMER_CLAIM` starting with `OIDC_CUSTOMER_PREFIX` become customer authorities (`/Customers/ACME` → `ROLE_CUSTOMER_ACME`).

The frontend fetches these values from `GET /api/public/config` and runs a standard authorization code + PKCE flow with silent refresh-token renewal. Redirect URIs to register with the IdP: `https://<app-host>/auth/callback` (login) and `https://<app-host>/sign-in` (post-logout).

### Keycloak

```env
OIDC_ISSUER_URI=https://<keycloak-host>/realms/<realm>
OIDC_CLIENT_ID=<spa-client-id>
OIDC_SCOPES=openid profile
```

The claim variables can stay at their defaults, provided the realm is set up like `dev-realm.json`: a public SPA client (standard flow + PKCE S256), a client-role mapper that puts roles in a top-level `roles` claim, and a group membership mapper that puts group paths in a `groups` claim (customers as subgroups of `/Customers`). Optionally set `OIDC_AUDIENCE` if the client has an audience mapper.

If the app cannot reach the public issuer URL internally (e.g. IdP behind the same reverse proxy), set `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` to the internally reachable JWKS endpoint; issuer validation still uses `OIDC_ISSUER_URI`.

### Microsoft Entra ID

One app registration is needed:

1. Add a **Single-page application** platform with redirect URI `https://<app-host>/auth/callback` and post-logout redirect URI `https://<app-host>/sign-in` (add `http://localhost:5173` variants for local testing).
2. Under **Expose an API**, set the Application ID URI to `api://{clientId}` and add a scope named `access_as_user`. This is mandatory: without a custom API scope in `OIDC_SCOPES`, Entra issues Microsoft Graph access tokens that the backend cannot validate.
3. Under **App roles**, create a role with value `Admin`, plus one role per customer with values like `Customer:ACME`. Assign users or groups to the roles via the Enterprise Application.
4. In the app manifest, set `api.requestedAccessTokenVersion` to `2` so access tokens carry the v2.0 issuer matching `OIDC_ISSUER_URI`.
5. Optionally add `given_name` and `family_name` as optional ID-token claims; the frontend falls back to `name` for the avatar initials.

```env
OIDC_ISSUER_URI=https://login.microsoftonline.com/{tenantId}/v2.0
OIDC_CLIENT_ID={clientId}
OIDC_SCOPES=openid profile offline_access api://{clientId}/access_as_user
OIDC_AUDIENCE={clientId}
OIDC_ROLES_CLAIM=roles
OIDC_CUSTOMER_CLAIM=roles
OIDC_CUSTOMER_PREFIX=Customer:
```

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
