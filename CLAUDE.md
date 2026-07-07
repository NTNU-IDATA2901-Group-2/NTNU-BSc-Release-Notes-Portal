# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Release Notes Portal: a full-stack app for creating, editing, and publishing release notes, which are assembled from change notes synced out of git repositories, with Jira lookups and AI-assisted summarization/translation.

## Tech Stack

- Backend: Spring Boot 4, Java 25, Maven (`./mvnw`), Spring Data JPA, Spring AI (any OpenAI-compatible endpoint, configured via `OPENAI_*` env vars), JGit, PostgreSQL (H2 in `ci` profile)
- Frontend (`frontend/`): Vue 3 + TypeScript + Vite, pnpm, Tailwind v4, reka-ui (shadcn-vue style), TanStack vue-query/vue-table, vee-validate + zod, oidc-client-ts, vue-i18n (en-GB/nb-NO/fr-FR), pdfmake
- Auth: any OIDC IdP (dev uses Keycloak from `docker-compose.yml`); API tests via Bruno CLI

## Key Commands

```bash
# Backend dev (auto-starts Postgres + Keycloak via spring-boot-docker-compose; needs .env)
./mvnw spring-boot:run                     # http://localhost:8080

# Frontend dev
cd frontend && pnpm install && pnpm dev    # http://localhost:5173

# Lint + type-check/build (what CI runs; there is NO "lint" npm script — run the binary)
cd frontend && pnpm eslint && pnpm build   # build = vue-tsc -b && vite build

# Backend tests (all / single class / single method)
./mvnw test
./mvnw test -Dtest=JwtRolesGrantedAuthoritiesConverterTest
./mvnw test -Dtest=JwtRolesGrantedAuthoritiesConverterTest#methodName

# Full package (frontend must be built into static/ first for a servable JAR)
cd frontend && pnpm build && cd .. \
  && mkdir -p src/main/resources/static && cp -r frontend/dist/* src/main/resources/static/ \
  && ./mvnw clean package

# API tests (Bruno) — run against the app started with the ci profile
java -jar target/*.jar --spring.profiles.active=ci
cd test/ReleaseNoteAPITests && bru run --env Local --sandbox=developer
```

Dev first-run: copy `.env.example` → `.env`, fill it, then import `dev-realm.json` into Keycloak (`http://localhost:8081`) — see README for the full checklist.

## Architecture

Single-JAR deployment: the Vue build is copied into `src/main/resources/static/` and served by `SpaWebMvcConfig`. Root package `no.reliablesolutions.release_notes_portal`, layered `controller → service → domain/{entity,repository}`, with `config/` (security), `runner/` (startup sync), `util/` (git/YAML/AI helpers).

### Domain model
- `ChangeNote` is central: optional `@ManyToOne` to `Product`/`Scope`/`Feature`/`Customer`, tied to a `GitRepository` + commit hash (unique per repo), flags `published`/`archived`/`viewableByEveryone`.
- `ReleaseNote` owns a `@ManyToMany` to `ChangeNote`, an embedded `ReleaseTimeline`, and cascade-owned `ChangeImpact` children; flag `syncedToGit`.
- `Prompt` rows (seeded by `data.sql`) hold the AI prompts, looked up by name ("Translation Prompt", "Change Notes Summary") — AI features break without seed data.

### Git sync (both directions)
- Inbound: `runner/ChangeNotesSyncHandler` (startup + on-demand) clones/fetches each `GitRepository` into `git_repositories/<name>`, walks commits since `lastCheckedCommitHash`, and parses **added** `.yaml`/`.yml` files in the repo's change-note directory into `ChangeNote`s via `util/ChangeNoteFileHandler` (SnakeYAML; resolves tag entities by name).
- Outbound: `util/ReleaseNoteSyncHandler` commits release-note YAML to a `release-note-<id>` branch and pushes with the repo's PAT, rolling back branches on failure.

### Security & profiles
- `dev`/`prod`: stateless OIDC resource server (`SecurityConfig`). `JwtRolesGrantedAuthoritiesConverter` maps the roles claim → `ROLE_*` and customer-claim entries with the configured prefix → `ROLE_CUSTOMER_*` (claim names via `AuthClaimsProperties` / `OIDC_*` env). Services filter data per admin-vs-customer via `AuthenticationUtil`/`AccessScope`.
- `ci`: H2, no docker-compose, `SecurityConfigCi` permits all and synthesizes roles from `X-Test-Role`/`X-Test-Customers` headers (what Bruno tests use). Git, Jira, and AI beans are `@Profile("!ci")` and consumed via `ObjectProvider`.
- Default profile is `dev` (set in `application.properties`); `.env` is loaded through `spring.config.import`.

### Frontend
- Runtime config: `utils/constants.ts` uses `VITE_*` env in dev but top-level-awaits `GET /api/public/config` in prod — new runtime config values need both `ConfigController` and `constants.ts`.
- No Vite proxy: dev frontend calls `http://localhost:8080/api/` directly (CORS allows `localhost:5173`). Shared axios instance in `api/api.ts` attaches the bearer token; one `api/*-api.ts` module per domain.
- Auth in `utils/auth.ts` (PKCE, silent renew, reactive `isAuthenticated`/`isAdmin`); router guards use `meta.requiresAuth`.
- `components/ui/**` is generated shadcn-style reka-ui code and is excluded from ESLint — don't hand-refactor it; domain components live in `components/{changeNote,releaseNote,filters}`.

## CI (`.github/workflows/build-test-push.yml`)

PRs to `main`/`dev`: frontend eslint + build → `mvn clean package` (with frontend dist baked in) → boot JAR with `ci` profile → Bruno API tests. Merged PRs to `main` additionally build/push a multi-arch Docker image to ghcr.io.

## Gotchas

- `pnpm eslint` is not a package.json script — it invokes the eslint binary directly (flat config `frontend/eslint.config.js`). CI fails on any lint error.
- New backend beans touching git, Jira, or AI must be `@Profile("!ci")` (and injected via `ObjectProvider` where consumed), or the CI jar won't start.
- JGit: `Git.open()`'s `git.close()` already closes the underlying `Repository` — don't also close `git.getRepository()` in try-with-resources.
- reka-ui `Select`: render the label inside the `SelectValue` slot, otherwise a pre-selected value shows the placeholder. reka-ui `Calendar`: week start comes from the locale (use e.g. `en-GB`), not a `weekStartsOn` prop.
- PDF export (`utils/pdf.ts`) has two variants gated on `PdfVariant`: the customer-facing PDF must omit developer/upgrade notes; the technical PDF includes them.
- Enums used by only one entity are nested inside that entity class (e.g. `TestingNeed`) — follow that convention.
- Commit style: conventional commits with gitmoji, e.g. `feat: 📱 …`, `chore(logging): 🔊 …`.
- Local machine quirks: `docker compose` can leave `target/` root-owned (run Maven from a copy if `clean` fails); default node may be v18 — Vite needs v20+ (`nvm use 22`).
