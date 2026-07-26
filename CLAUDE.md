# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A customer support ticketing system: customer emails become tickets, AI categorizes them and drafts replies from an admin-uploaded knowledge base (PDF/Word), and a single admin reviews/approves drafts or handles escalations from a login-gated dashboard. Customers have no login — email only.

Full MVP scope, ticket statuses, and what's explicitly out of scope for the MVP live in `PROJECT_PLAN.md`. Tech choices and rationale live in `TECH_STACK.md`. Read both before making architectural decisions — many "obvious" additions (multi-admin, SLA tracking, microservices, message queues, cloud object storage) are explicitly deferred.

This is a two-service repo with no shared root tooling: `backend/` (Spring Boot) and `frontend/` (React/Vite), each with its own build.

## Commands

### Backend (`backend/`)
- Run dev server: `./mvnw spring-boot:run` — starts on `http://localhost:8080`, requires PostgreSQL reachable at `localhost:5432` (db `support_system`, see `application.properties`)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=ClassName`
- Run a single test method: `./mvnw test -Dtest=ClassName#methodName`
- Build jar: `./mvnw package`
- No Spring Boot DevTools — the backend must be manually stopped/restarted after any Java source change (no hot reload).

### Frontend (`frontend/`)
- Run dev server: `npm run dev` — starts on `http://localhost:5173`
- Build: `npm run build`
- Lint: `npm run lint` (oxlint)
- Preview production build: `npm run preview`

### Running both together
Start backend and frontend dev servers separately (different processes/ports). The Vite dev server proxies `/api` to `http://localhost:8080` (see `frontend/vite.config.js`), so frontend code should call same-origin `/api/...` paths, not `http://localhost:8080/...` directly.

## Architecture

**Backend**: Spring Boot (Java 21, Maven), package root `com.supportsystem.backend`. Structure so far is feature-packages under the root (e.g. `health/`, `config/`) rather than layered `controller/service/repository` packages — follow that convention (group by feature, not by layer) as new features are added.

- **Security**: Spring Security is on the classpath and secures *every* endpoint by default (including a generated-password admin login logged on startup). There is no global permissive config — `SecurityConfig` (`config/SecurityConfig.java`) explicitly allowlists individual public paths (currently just `/api/health`) via `requestMatchers(...).permitAll()`, with everything else falling through to `.anyRequest().authenticated()`. Any new public endpoint (e.g. the inbound email webhook) must be added to this allowlist explicitly; it will otherwise 401.
- **Persistence**: Spring Data JPA + PostgreSQL, `spring.jpa.hibernate.ddl-auto=update` (schema auto-updates from entities in dev — no migration tool set up yet).
- Per `TECH_STACK.md`: no microservices, no message broker (webhook triggers processing synchronously/directly), no API gateway, no cloud object storage — keep additions consistent with this simplicity bias.

**Frontend**: React 19 + Vite + TailwindCSS 4 (via `@tailwindcss/vite` plugin, imported in `src/index.css` with `@import "tailwindcss"` — no `tailwind.config.js`). Plain JS (`.jsx`, not TypeScript). Linting is via `oxlint`, not ESLint (see `.oxlintrc.json`).

## Conventions

- Tests: `./mvnw test` on the backend runs JUnit 5 tests (already in `pom.xml` via `spring-boot-starter-webmvc-test`/`-security-test`) under `backend/src/test/java/com/supportsystem/backend/`, mirroring the feature-package convention — e.g. `auth/AuthControllerTest.java` (plain unit test) and `auth/AuthIntegrationTest.java` (MockMvc, against the real local Postgres — no H2/testcontainers).
- Frontend has two separate suites. Unit/integration: Vitest + React Testing Library, run via `npm run test`; config is in `vite.config.js`'s `test` block + `src/setupTests.js`; `*.test.jsx` files are colocated next to what they test (e.g. `src/auth/AuthContext.test.jsx`), except `src/App.integration.test.jsx`, the one cross-cutting test that renders the real router/pages together. The mocking seam is always `src/api/auth.js` (never `axios`/`src/api/axios.js` directly) — follow that convention for future API modules. E2E: Playwright, run via `npm run test:e2e` (config `playwright.config.js`, specs in `e2e/*.spec.js`); requires the real backend + frontend dev servers and Postgres already running (no auto-started `webServer`). One-time browser install: `npx playwright install chromium` (the bare `npx playwright install` also tries WebKit, which fails on macOS 13/Ventura).
