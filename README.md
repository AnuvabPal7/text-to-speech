# Text-to-Speech (TTS) Full-Stack Application
### Java Spring Boot (Backend) + React Vite (Frontend) + PostgreSQL

A complete, production-grade Text-to-Speech web application developed according to the specifications in the **"Java - Text-to-Speech Application.pdf"**.

**🔗 Live Demo:** https://text-to-speech-ashen-psi.vercel.app
**🔗 Backend Health Check:** https://text-to-speech-lttm.onrender.com/api/health

> Backend is hosted on Render's free tier, which spins down after periods of inactivity. The first request after idle time may take 30–60 seconds to respond while the instance wakes up.

---

## 1. Architecture & Technology Stack

```
text-to-speech/
├── backend/                     # Java Spring Boot Maven Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tts/
│   │   │   │   ├── config/      # CORS, Storage, Rate Limit, and TTS Properties
│   │   │   │   ├── controller/  # REST Controllers (TTS, Voices, Health, History)
│   │   │   │   ├── dto/         # Request, Response, and Health DTOs
│   │   │   │   ├── exception/   # Global Exception Handler & Custom Exceptions
│   │   │   │   ├── model/       # JPA Entities (SpeechHistory)
│   │   │   │   ├── ratelimit/   # Per-IP sliding-window rate limiter (guards POST /api/tts)
│   │   │   │   ├── repository/  # Spring Data JPA Repositories
│   │   │   │   ├── service/     # TTS Synthesis, Voice Registry, History & Audio Cleanup Services
│   │   │   │   └── TtsApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── schema.sql
│   │   │       └── db/migration/V1__create_speech_history_table.sql
│   │   └── test/                # MockMvc & JUnit 5 Integration Test Suite
│   ├── Dockerfile               # Multi-stage build (Render has no native Java runtime)
│   ├── .dockerignore
│   └── pom.xml                  # Maven Build Descriptor (Java 17)
├── frontend/                    # React 18+ Vite Single Page Application
│   ├── src/
│   │   ├── components/          # Modular React UI Components
│   │   ├── services/            # Fetch-based API Service Layer
│   │   ├── types/                # TypeScript Definitions
│   │   ├── App.tsx
│   │   ├── main.tsx
│   │   └── vite-env.d.ts        # Required for import.meta.env typing
│   ├── .env.example
│   ├── package.json
│   └── vite.config.ts
├── postman/                     # Postman Test Suite
│   └── TTS_API_Collection.postman_collection.json
├── .gitignore
└── README.md
```

### Backend
* **Java**: 17 LTS
* **Framework**: Spring Boot 3.3.x (Spring Web, Spring Boot Validation, Spring Data JPA / Hibernate)
* **Build Tool**: Apache Maven (`pom.xml`)
* **Database**: PostgreSQL (Supabase) with Hibernate ORM and Flyway database migrations
* **Test Suite**: JUnit 5, Spring Boot Test, Spring MockMvc
* **Rate Limiting**: In-memory sliding-window limiter (per client IP) protecting `POST /api/tts`
* **Audio Cleanup**: Scheduled job deletes generated audio files older than a configurable retention window
* **Deployment**: Docker (Render has no native Java runtime — see `backend/Dockerfile`)

### Frontend
* **UI Framework**: React 18+ with TypeScript
* **Tooling**: Vite
* **Styling**: Tailwind CSS
* **Icons**: Lucide React
* **Components**: TextInput, LanguageSelector, VoiceSelector, GenerateButton, AudioPlayer, DownloadButton, ErrorMessage, SpeechHistoryList
* **Deployment**: Vercel

---

## 2. Text-to-Speech Provider

### Selected Provider: **AWS Amazon Polly**

#### Why AWS Amazon Polly?
1. **Multilingual Quality**: Industry-leading natural neural and standard voices across English (`en-US`, `en-GB`, `en-IN`), Hindi (`hi-IN`), Spanish (`es-ES`, `es-MX`), French (`fr-FR`), and German (`de-DE`).
2. **Standard AWS SDK v2**: Uses AWS SDK for Java v2 (`software.amazon.awssdk:polly`), providing native control over voice and output format.
3. **Generous Free Tier**: AWS offers **5,000,000 free characters/month** for standard voices and **1,000,000 free characters/month** for neural voices.
4. **Built-in Fallback / Local Development Mode**: If no AWS credentials are provided, the backend generates a synthesized placeholder audio file so you can test the full flow end-to-end without AWS access.

### Supported Languages & Voices

This table reflects what's actually registered in `VoiceService.java` — not the original spec doc, which listed a couple of languages (Gujarati, Marathi) that were never implemented, since AWS Polly itself doesn't offer those languages.

| Language | Code | Voices Available | Engine |
|---|---|---|---|
| **English (US)** | `en-US` | Joanna, Matthew, Kendra, Salli | Neural |
| **English (UK)** | `en-GB` | Amy, Brian, Arthur | Neural |
| **English (India)** | `en-IN` | Kajal (Neural), Aditi (Standard), Raveena (Standard) | Mixed |
| **Hindi (India)** | `hi-IN` | Kajal (Neural), Aditi (Standard) | Mixed |
| **Spanish (Spain)** | `es-ES` | Lucia (Neural), Enrique (Standard) | Mixed |
| **Spanish (Mexico)** | `es-MX` | Mia, Andres | Neural |
| **French** | `fr-FR` | Lea, Remi | Neural |
| **German** | `de-DE` | Vicki, Daniel | Neural |

> **Known gap:** the backend supports `es-MX` (Spanish Mexico) as shown above, but the frontend's language dropdown (`DEFAULT_LANGUAGES` in `services/api.ts`) doesn't currently list it as a selectable option — only `es-ES` is exposed in the UI. The API itself works fine for `es-MX` if called directly (e.g. via Postman).

---

## 3. Environment Variables

### Backend (set on Render, or locally via OS/IDE — no `.env` file used for the backend)

**Required:**
```
TS_PASSWORD                  # Supabase/PostgreSQL database password
TTS_AWS_ACCESS_KEY_ID        # AWS access key
TTS_AWS_SECRET_ACCESS_KEY    # AWS secret key
```

**CORS (required once you have a deployed frontend URL):**
```
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

**Optional (all have sensible defaults):**
```properties
# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/tts_db}
spring.datasource.username=${DB_USERNAME:postgres}

# AWS Polly
aws.region=${TTS_AWS_REGION:ap-northeast-2}
aws.polly.default-voice=${TTS_AWS_POLLY_VOICE:Joanna}
aws.polly.output-format=${TTS_AWS_POLLY_OUTPUT_FORMAT:mp3}
aws.polly.engine=${TTS_AWS_POLLY_ENGINE:standard}

# Text to Speech
tts.provider=${TTS_PROVIDER:polly}
tts.max-text-length=5000
tts.storage-dir=${TTS_STORAGE_DIR:./storage/audio}
tts.retention-hours=${TTS_AUDIO_RETENTION_HOURS:24}   # generated audio files older than this are auto-deleted

# Rate Limiting (protects the paid POST /api/tts endpoint)
tts.rate-limit.enabled=${TTS_RATE_LIMIT_ENABLED:true}
tts.rate-limit.requests-per-window=${TTS_RATE_LIMIT_MAX:10}
tts.rate-limit.window-seconds=${TTS_RATE_LIMIT_WINDOW_SECONDS:60}
```

### Frontend (set on Vercel, or locally in `frontend/.env`)

```
VITE_API_BASE_URL=https://your-backend.onrender.com
```

Leave this unset for local development — Vite's dev server proxy (`vite.config.ts`) forwards `/api` to `http://localhost:8080` automatically. It's required in production because there's no such proxy on Vercel; without it, the frontend would try to call itself instead of the real backend. See `frontend/.env.example`.

---

## 4. Deployment

### Backend → Render (Docker)
1. New Web Service → connect this repo.
2. **Root Directory**: `backend`
3. **Runtime**: Docker (Render has no native Java/JVM runtime — see `backend/Dockerfile`)
4. **Dockerfile Path**: `Dockerfile`
5. **Docker Build Context Directory**: `backend`
6. Set the required environment variables listed above.
7. Deploy, then verify: `curl https://your-backend.onrender.com/api/health`

### Frontend → Vercel
1. New Project → connect this repo.
2. **Root Directory**: `frontend`
3. Framework preset: Vite (auto-detected)
4. Add environment variable `VITE_API_BASE_URL` = your Render backend URL.
5. Deploy.
6. Go back to Render and set `CORS_ALLOWED_ORIGINS` to your new Vercel URL, then redeploy the backend.

---

## 5. Database Setup (Supabase PostgreSQL & Flyway)

The application is configured for **Supabase PostgreSQL** using **Flyway** for migrations.

### Required Supabase Connection Details:
1. **DB URL (`DB_URL`)**: `jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require` (connection pooler, recommended)
2. **DB Username (`DB_USERNAME`)**: `postgres.[project-ref]` (pooler) or `postgres` (direct)
3. **DB Password (`TS_PASSWORD`)**: your Supabase database password
4. **HikariCP settings** are pre-tuned in `application.properties` for Supabase's connection limits (max pool size 5, short max-lifetime to avoid stale PgBouncer connections)

### Schema
- Flyway is the sole migration manager (`src/main/resources/db/migration/V1__create_speech_history_table.sql`).
- `spring.sql.init.mode=never` prevents the legacy `schema.sql` from clashing with Flyway.

```sql
CREATE TABLE IF NOT EXISTS speech_history (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    language VARCHAR(30) NOT NULL,
    voice VARCHAR(100) NOT NULL,
    audio_url VARCHAR(500) NOT NULL,
    character_count INT NOT NULL,
    word_count INT NOT NULL,
    audio_format VARCHAR(10) DEFAULT 'mp3',
    file_size_bytes BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_speech_history_created_at ON speech_history (created_at DESC);
```

---

## 6. Running Locally

### Backend
```bash
cd backend
# Set TS_PASSWORD, TTS_AWS_ACCESS_KEY_ID, TTS_AWS_SECRET_ACCESS_KEY via your OS/IDE first
mvn clean compile spring-boot:run
```
Starts on `http://localhost:8080`. Verify: `curl http://localhost:8080/api/health`

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Starts on `http://localhost:5173` (Vite default) and proxies `/api` to the backend automatically — no `VITE_API_BASE_URL` needed locally.

---

## 7. REST API Documentation

### 1. Generate Speech
* **Endpoint**: `POST /api/tts` — rate-limited to 10 requests/minute per client IP by default
* **Content-Type**: `application/json`
* **Request Body**:
  ```json
  {
    "text": "Hello! Welcome to our Text to Speech application.",
    "language": "en-US",
    "voice": "Joanna"
  }
  ```
* **Success Response (200 OK)**:
  ```json
  {
    "success": true,
    "audioUrl": "/api/audio/7a1f59e1-2c09-4171-8bc8-43d9225c2763.mp3",
    "message": "Audio successfully generated.",
    "historyId": 1,
    "characterCount": 49,
    "wordCount": 7,
    "format": "mp3",
    "fileSizeBytes": 32768
  }
  ```
  Note: `audioUrl` is relative to the backend's own origin, not the frontend's — the frontend resolves it via `resolveAudioUrl()` in `services/api.ts`.
* **Rate Limit Response (429 Too Many Requests)**: returned with a `Retry-After` header once the per-IP limit is exceeded.

### 2. Available Voices
* **Endpoint**: `GET /api/voices` or `GET /api/voices?language=en-US`

### 3. Backend Health
* **Endpoint**: `GET /api/health`
* Returns live status — checks an actual database connection, not a hardcoded value.

### 4. Audio Streaming & Download
* **Endpoint**: `GET /api/audio/{filename}`
* Streams binary audio with `Accept-Ranges: bytes`. Files older than `tts.retention-hours` (default 24h) are automatically deleted by a scheduled cleanup job.

### 5. Speech History
* **Endpoint**: `GET /api/history`, `DELETE /api/history/{id}`, `DELETE /api/history`

### HTTP Status Codes
* `200 OK` — Successful synthesis, voices list, health check, history deleted.
* `400 Bad Request` — Empty text, character limit exceeded (>5000), invalid language, mismatched voice/language.
* `404 Not Found` — Audio file or history record not found.
* `413 Payload Too Large` — Request body exceeds size limit.
* `429 Too Many Requests` — Rate limit exceeded on `POST /api/tts`.
* `500 Internal Server Error` — Unexpected backend exception.
* `503 Service Unavailable` — External TTS provider outage or network unreachable.

---

## 8. Testing Guide

### Automated JUnit & MockMvc Tests
```bash
cd backend
mvn test
```
Covers: context loading, TTS fallback synthesis, `/api/health`, `/api/voices` (with and without language filter), `POST /api/tts` (success + 4 validation failure cases), and full CRUD on `/api/history`.

### Postman Testing
1. Import `postman/TTS_API_Collection.postman_collection.json`.
2. Set `baseUrl` to `http://localhost:8080` for local testing, or the live Render URL for production testing.
3. Run requests to verify positive flows and error status codes.

---

## 9. Known Limitations

* **Ephemeral storage on Render free tier**: generated audio files live on local disk. A Render restart (common on the free tier after idle spin-down) wipes files that hadn't already expired via the retention job. Migrating to S3-backed storage (as done in a related project, SecureSign) would fix this permanently.
* **`es-MX` not selectable in the UI**: the backend fully supports Spanish (Mexico) voices, but the frontend's language dropdown doesn't currently list it — see Section 2.
* **No authentication**: history is global, not per-user. Adding JWT-based auth (as in SecureSign) would be needed for multi-tenant use.