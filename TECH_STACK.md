# Tech Stack

## Backend
- Java + Spring Boot — REST API, business logic
- Spring Security — admin login/auth
- Spring Data JPA — database access

## Frontend
- React + Vite
- TailwindCSS

## Database
- PostgreSQL

## Supporting Pieces (needed by the MVP plan, simplest default chosen — swap as needed)
- Email: SendGrid — outbound sending via the Mail Send API, inbound customer email via the Inbound Parse webhook
- AI: Claude API (Anthropic) for categorization + drafting replies — assumption
- KB file storage: uploaded PDF/Word docs stored as files on disk (or a single DB table with the file blob) — no cloud storage service in MVP, kept simple
- Auth: session-based login for the single admin user (no OAuth/SSO in MVP)

## Explicitly Not Used (keeping it simple)
- No microservices — single Spring Boot app
- No message queue/broker — webhook triggers processing directly
- No separate API gateway/BFF layer
- No cloud object storage (S3 etc.) for MVP — plain disk/DB storage is enough at this scale
