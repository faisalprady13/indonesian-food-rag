# Food RAG

A full-stack RAG (retrieval-augmented generation) chatbot for exploring food recipes. Users chat in natural language, and the assistant retrieves relevant recipes from a Postgres/pgvector store, answers questions about them, and can search, look up, and manage favorites on the user's behalf via tool calling.

Each user brings their own OpenAI API key (stored encrypted), so there is no shared/global LLM billing.

## Features

- **Recipe RAG chat** — semantic search over recipe embeddings (pgvector) + OpenAI chat completion, with tool calling for recipe search, exact-title lookup, and favorites management.
- **Conversation context memory** — each conversation remembers the recipe IDs behind the last numbered list it showed (search results, favorites, ...), so follow-ups like "save number 2" resolve correctly without relying on the LLM to recall the mapping. Context is cascade-deleted with its conversation.
- **Conversations** — persisted chat history per user, with rename, pin/unpin, and delete. Conversation titles are auto-generated from the first message.
- **Recipe browsing** — paginated, sortable, searchable recipe list with autocomplete and a favorites collection.
- **Auth** — local email/password (JWT) plus Google and GitHub OAuth2 login.
- **Bring-your-own API key** — each user stores their own OpenAI API key (encrypted at rest); the backend builds a per-user chat/embedding client from it, so there's no shared OpenAI billing.
- **Profile management** — update profile info and upload a profile picture (via Cloudinary).
- **Theme preference** — light/dark app theme stored per user.

## Tech stack

**Backend** — Java 25, Spring Boot 4, Spring AI (OpenAI chat + embeddings), Spring Data JPA, Spring Security (JWT + OAuth2), PostgreSQL + pgvector, Flyway, Cloudinary SDK, Lombok.

**Frontend** — React 19, TypeScript, Vite, TanStack Query, React Router, Zustand, Tailwind CSS, Base UI + shadcn-style components.

**Data** — [Supabase](https://supabase.com/) hosts the PostgreSQL database (with the `vector` extension enabled) used for both local and deployed environments.

**Infra** — Docker (multi-stage build: frontend is built and copied into the backend jar as static resources — a single container serves both), GitHub Actions for CI (build + lint) and CD (build/push image, trigger deployment webhook).

## Project structure

```
backend/    Spring Boot API (org.myspring.backend: controller, service, repository, model, tool, security, config)
frontend/   React + Vite SPA (src/components, src/pages, src/queries, src/store)
Dockerfile          Multi-stage build: frontend -> static resources -> backend jar
docker-compose.yml   Postgres + backend for local/self-hosted runs
```

## Getting started

### Prerequisites

- Java 25 and Maven (or use the bundled `./mvnw`)
- Node.js 22.17.0+ and npm
- PostgreSQL with the `vector` extension available — the Docker Compose setup handles this for local runs; the hosted deployment uses a [Supabase](https://supabase.com/) Postgres instance
- OAuth2 app credentials (Google and/or GitHub) if you want social login
- A Cloudinary account if you want profile picture uploads to work

### Environment variables

Copy `.env.example` to `.env` and fill it in:

| Variable | Purpose |
|---|---|
| `POSTGRES_DB`, `POSTGRES_DB_URL`, `POSTGRES_DB_USER`, `POSTGRES_DB_PASSWORD` | Database connection (point these at a Supabase connection string for a hosted Postgres instance, or at the Docker Compose Postgres for local runs) |
| `GITHUB_ID`, `GITHUB_SECRET` | GitHub OAuth2 app credentials |
| `GOOGLE_ID`, `GOOGLE_SECRET` | Google OAuth2 app credentials |
| `FRONTEND_URL` | Frontend origin, used for OAuth2 redirect/CORS |
| `JWT_SECRET` | Signing secret for JWTs (`JWT_EXPIRATION_MS` optionally overrides the default 24h expiry) |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Profile image uploads |
| `ENCRYPTION_PASSWORD`, `ENCRYPTION_SALT` | Encrypt/decrypt users' stored OpenAI API keys |

Note: there is no global OpenAI API key — each user provides their own key from the app's settings, and it's encrypted at rest using `ENCRYPTION_PASSWORD`/`ENCRYPTION_SALT`.

### Run with Docker Compose

```bash
cp .env.example .env   # fill in the values
docker compose up --build
```

The app is served entirely at `http://localhost:8080` (the backend serves the built frontend as static resources).

### Run manually (development)

Backend (needs a Postgres instance with the `vector` extension and the env vars above set):

```bash
cd backend
./mvnw spring-boot:run
```

Frontend (dev server proxies `/api` to `http://localhost:8080`):

```bash
cd frontend
npm ci
npm run dev
```

## API overview

All endpoints below are under `/api`. `/api/auth/register`, `/api/auth/login`, and `/oauth2/**` are public; everything else under `/api/**` requires authentication (JWT).

**Auth** — `/api/auth`
- `GET /api/auth` — current authenticated user
- `POST /api/auth/register` — register
- `POST /api/auth/login` — login, returns a JWT

**Conversations** — `/api/conversation`
- `GET /api/conversation` — list conversations (newest updated first)
- `GET /api/conversation/{id}` — conversation detail with messages
- `PATCH /api/conversation/{id}` — rename
- `PATCH /api/conversation/{id}/pinned` — pin/unpin
- `DELETE /api/conversation/{id}` — delete

**Recipes** — `/api/recipe`
- `GET /api/recipe` — paginated/sortable/searchable list
- `GET /api/recipe/{id}` — detail
- `GET /api/recipe/autocomplete` — title autocomplete
- `POST /api/recipe/{id}/favorite` / `DELETE /api/recipe/{id}/favorite` — add/remove favorite
- `GET /api/recipe/favorites` — paginated favorites
- `POST /api/recipe/ask` — RAG chat endpoint

**User** — `/api/user`
- `PUT /api/user/{id}` — update profile (multipart, optional profile image)
- `DELETE /api/user/{id}/{username}` — delete account

**User settings** — `/api/user-setting`
- `GET /api/user-setting` — get theme/settings
- `PUT /api/user-setting/` — update theme and/or OpenAI API key

## Database migrations

Schema is managed with Flyway (`backend/src/main/resources/db/migration`); Hibernate runs in `validate` mode only, so schema changes go through a new migration file, not `ddl-auto`.

## CI/CD

- **CI** (`.github/workflows/ci.yml`) — on push/PR to `main`: builds the backend with Maven and lints/builds the frontend.
- **CD** (`.github/workflows/cd.yml`) — on push to `main`: builds and pushes the Docker image, then triggers a deployment webhook.

## Acknowledgements

Recipe data (titles, ingredients, instructions, and images) used to seed the `recipes`/`ingredients` tables comes from the [Food Ingredients and Recipe Dataset with Images](https://www.kaggle.com/datasets/pes12017000148/food-ingredients-and-recipe-dataset-with-images) on Kaggle, licensed under [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0/).