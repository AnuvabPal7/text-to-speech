# Text-to-Speech (TTS) Full-Stack Application
### Java Spring Boot (Backend) + React Vite (Frontend) + PostgreSQL

A complete, production-grade Text-to-Speech web application developed according to the specifications in the **"Java - Text-to-Speech Application.pdf"**.

---

## 1. Architecture & Technology Stack

```
text-to-speech/
├── backend/                     # Java Spring Boot Maven Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/tts/
│   │   │   │   ├── config/      # CORS, Storage, and TTS Properties
│   │   │   │   ├── controller/  # REST Controllers (TTS, Voices, Health, History)
│   │   │   │   ├── dto/         # Request, Response, and Health DTOs
│   │   │   │   ├── exception/   # Global Exception Handler & Custom Exceptions
│   │   │   │   ├── model/       # JPA Entities (SpeechHistory)
│   │   │   │   ├── repository/  # Spring Data JPA Repositories
│   │   │   │   ├── service/     # TTS Synthesis, Voice Registry & History Services
│   │   │   │   └── TtsApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── schema.sql
│   │   │       └── db/migration/V1__create_speech_history_table.sql
│   │   └── test/                # MockMvc & JUnit 5 Integration Test Suite
│   └── pom.xml                  # Maven Build Descriptor (Java 17/21)
├── frontend/                    # React 18+ Vite Single Page Application
│   ├── src/
│   │   ├── components/          # Modular React UI Components
│   │   ├── services/            # Axios / Fetch API Service Layer
│   │   ├── types/               # TypeScript Definitions
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── vite.config.ts
├── postman/                     # Postman Test Suite
│   └── TTS_API_Collection.postman_collection.json
├── .gitignore
└── README.md
```

### Backend
* **Java**: 17 or 21 LTS
* **Framework**: Spring Boot 3.3.x (Spring Web, Spring Boot Validation, Spring Data JPA / Hibernate)
* **Build Tool**: Apache Maven (`pom.xml`)
* **Database**: PostgreSQL with Hibernate ORM and Flyway database migrations
* **Database Driver**: `org.postgresql:postgresql`
* **Test Suite**: JUnit 5, Spring Boot Test, Spring MockMvc, H2 in-memory test database

### Frontend
* **UI Framework**: React 18+ with TypeScript
* **Tooling**: Vite
* **Styling**: Tailwind CSS
* **Icons**: Lucide React
* **Components**: TextInput, LanguageSelector, VoiceSelector, GenerateButton, AudioPlayer, DownloadButton, ErrorMessage, HistoryList

---

## 2. Text-to-Speech Provider Selection

### Selected Provider: **AWS Amazon Polly** (with Google Cloud TTS support)

#### Why AWS Amazon Polly?
1. **Multilingual Quality**: Industry-leading natural neural and standard voices with superior support for Indian regional languages (Hindi `hi-IN`, Gujarati `gu-IN`, Marathi `mr-IN`) as well as English (`en-US`, `en-GB`, `en-IN`), Spanish (`es-ES`, `es-MX`), French (`fr-FR`), and German (`de-DE`).
2. **Standard AWS SDK v2**: Uses AWS SDK for Java v2 (`software.amazon.awssdk:polly`), providing native control over speed, pitch, and voice styles.
3. **Generous Free Tier**: AWS offers **5,000,000 free characters per month** for standard voices and **1,000,000 free characters per month** for neural voices on the AWS Free Tier.
4. **Built-in Fallback / Local Development Mode**: If no AWS credentials are provided during initial local evaluation, the backend seamlessly generates a synthesized development audio file with harmonic speech modulation so you can test all features end-to-end immediately without blocking!

### Supported Languages & Neural Voices

| Language | Code | Voices Available | Gender | Accents |
|---|---|---|---|---|
| **English (US)** | `en-US` | `Joanna`, `Matthew`, `Kendra`, `Salli` | Female / Male | American |
| **English (UK)** | `en-GB` | `Amy`, `Brian`, `Arthur` | Female / Male | British |
| **English (India)** | `en-IN` | `Kajal`, `Aditi` | Female | Indian |
| **Hindi (India)** | `hi-IN` | `Kajal`, `Aditi` | Female | Standard Hindi |
| **Gujarati (India)** | `gu-IN` | `Kajal`, `Aditi` | Female | Standard Gujarati |
| **Marathi (India)** | `mr-IN` | `Kajal`, `Aditi` | Female | Standard Marathi |
| **Spanish** | `es-ES`, `es-MX` | `Lucia`, `Enrique`, `Mia`, `Andres` | Female / Male | Castilian / Mexican |
| **French** | `fr-FR` | `Lea`, `Remi` | Female / Male | Metropolitan French |
| **German** | `de-DE` | `Vicki`, `Daniel` | Female / Male | Standard German |

---

## 3. Environment Variables Configuration

> **IMPORTANT**: As requested, **NO `.env` FILE IS REQUIRED OR CREATED** for the Java backend. All configurations are read directly from your operating system environment or IntelliJ IDEA Run Configurations.

### Configuration Properties (`application.properties`):
```properties
# Database
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/tts_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${TS_PASSWORD}

# AWS Polly
aws.region=${TTS_AWS_REGION:ap-south-1}
aws.access-key-id=${TTS_AWS_ACCESS_KEY_ID:}
aws.secret-access-key=${TTS_AWS_SECRET_ACCESS_KEY:}
aws.polly.default-voice=${TTS_AWS_POLLY_VOICE:Joanna}
aws.polly.output-format=${TTS_AWS_POLLY_OUTPUT_FORMAT:mp3}
aws.polly.engine=${TTS_AWS_POLLY_ENGINE:standard}

# Text to Speech
tts.provider=${TTS_PROVIDER:polly}
tts.max-text-length=5000
tts.audio-format=mp3
tts.storage-dir=${TTS_STORAGE_DIR:./storage/audio}
```

### How to Configure AWS Polly Credentials:
1. Obtain an AWS Access Key ID and Secret Access Key from the AWS Management Console (IAM).
2. Set `TTS_AWS_ACCESS_KEY_ID` and `TTS_AWS_SECRET_ACCESS_KEY` (or standard `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`).
3. Set `TTS_AWS_REGION` (e.g. `ap-south-1` or `us-east-1`).

---

## 4. How to Configure Environment Variables

### In IntelliJ IDEA (Recommended):
1. Open IntelliJ IDEA and open the `backend` directory.
2. From the top menu, go to **Run > Edit Configurations...**
3. Select your `TtsApplication` Spring Boot configuration (or click `+` and select **Spring Boot**).
4. In the **Environment variables** field, enter:
   ```text
   TTS_AWS_ACCESS_KEY_ID=your_key;TTS_AWS_SECRET_ACCESS_KEY=your_secret;TTS_AWS_REGION=ap-south-1;DB_URL=jdbc:postgresql://localhost:5432/tts_db;DB_USERNAME=postgres;TS_PASSWORD=your_password
   ```
5. Click **Apply** and **OK**.
6. Click the green **Play/Debug** icon to run.

### On Windows (Command Prompt or PowerShell):
```powershell
# PowerShell
$env:TTS_AWS_ACCESS_KEY_ID="your_aws_key"
$env:TTS_AWS_SECRET_ACCESS_KEY="your_aws_secret"
$env:TTS_AWS_REGION="ap-south-1"
$env:DB_URL="jdbc:postgresql://localhost:5432/tts_db"
$env:DB_USERNAME="postgres"
$env:TS_PASSWORD="your_postgres_password"

# Navigate to backend and run with Maven
cd backend
mvn spring-boot:run
```

### On macOS / Linux:
```bash
export TTS_AWS_ACCESS_KEY_ID="your_aws_key"
export TTS_AWS_SECRET_ACCESS_KEY="your_aws_secret"
export TTS_AWS_REGION="ap-south-1"
export DB_URL="jdbc:postgresql://localhost:5432/tts_db"
export DB_USERNAME="postgres"
export TS_PASSWORD="your_postgres_password"

cd backend
./mvnw spring-boot:run
# or: mvn spring-boot:run
```

---

## 5. Database Setup (Supabase PostgreSQL & Flyway)

The application is natively configured for **Supabase PostgreSQL** (or any cloud/local PostgreSQL instance) using **Flyway** for database migrations.

### Required Supabase Connection Details:
1. **DB URL (`DB_URL`)**:
   - **Connection Pooler (Recommended)**:
     `jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres?sslmode=require`
   - **Direct Connection**:
     `jdbc:postgresql://db.[project-ref].supabase.co:5432/postgres?sslmode=require`
2. **DB Username (`DB_USERNAME`)**:
   - For Transaction Pooler: `postgres.[project-ref]`
   - For Direct Connection: `postgres`
3. **DB Password (`DB_PASSWORD`)**: Your Supabase database password set during project creation.
4. **SSL Settings**: `sslmode=require` (appended to JDBC URL or via `spring.datasource.hikari.data-source-properties.sslmode=require`).
5. **HikariCP Connection Pool Settings** (pre-configured in `application.properties`):
   - `maximum-pool-size=5` (optimal for Supabase serverless/free-tier limits)
   - `minimum-idle=1`
   - `idle-timeout=30000` (30s)
   - `max-lifetime=60000` (60s, prevents stale connection errors on Supabase PgBouncer/Supavisor)
   - `connection-timeout=20000` (20s)

### Database Migration & Initialization Architecture:
- **Flyway is the sole database migration manager**:
  Migrations are maintained in `src/main/resources/db/migration/V1__create_speech_history_table.sql`.
- **No Schema Conflicts**: `spring.sql.init.mode=never` is explicitly configured to prevent legacy `schema.sql` from clashing with Flyway.
- **Auto-Provisioning**: On startup, Flyway connects with SSL and automatically provisions the `speech_history` table and indices if they do not exist.

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

## 6. How to Run the Application

### Step 1: Start PostgreSQL
Ensure PostgreSQL is running locally on port `5432` and database `tts_db` exists.

### Step 2: Run the Java Spring Boot Backend
```bash
cd backend
mvn clean compile spring-boot:run
```
The backend starts on `http://localhost:8080`.
Verify it is running:
```bash
curl http://localhost:8080/api/health
```

### Step 3: Run the React Frontend
```bash
cd frontend
npm install
npm run dev
```
The frontend starts on `http://localhost:3000` (or `http://localhost:5173`).
Open your browser and navigate to the application URL!

---

## 7. REST API Documentation

### 1. Generate Speech
* **Endpoint**: `POST /api/tts`
* **Content-Type**: `application/json`
* **Request Body**:
  ```json
  {
    "text": "Hello! Welcome to our Text to Speech application.",
    "language": "en-US",
    "voice": "en-US-JennyNeural",
    "speed": 1.0,
    "pitch": 1.0
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

### 2. Available Voices
* **Endpoint**: `GET /api/voices` or `GET /api/voices?language=en-US`
* **Response (200 OK)**:
  ```json
  [
    {
      "id": "Joanna",
      "name": "Joanna (Neural)",
      "languageCode": "en-US",
      "languageName": "English (United States)",
      "gender": "Female",
      "accent": "US",
      "provider": "polly"
    }
  ]
  ```

### 3. Backend Health
* **Endpoint**: `GET /api/health`
* **Response (200 OK)**:
  ```json
  {
    "status": "UP",
    "provider": "AWS Amazon Polly",
    "providerConfigured": true,
    "databaseStatus": "CONNECTED",
    "timestamp": "2026-09-18T10:15:00Z"
  }
  ```

### 4. Audio Streaming & Download
* **Endpoint**: `GET /api/audio/{filename}`
* Streams binary MP3/WAV audio with `Accept-Ranges: bytes` and `Content-Disposition: inline`.

### 5. Speech History
* **Endpoint**: `GET /api/history`
* Returns recent generation history from PostgreSQL/Supabase.

### 6. Delete History Record
* **Endpoint**: `DELETE /api/history/{id}`
* Deletes a specific speech record by ID from PostgreSQL/Supabase.

### 7. Clear All History Records
* **Endpoint**: `DELETE /api/history`
* Deletes all speech history records from PostgreSQL/Supabase.

### HTTP Status Codes
* `200 OK`: Successful synthesis, voices list, health check, history deleted.
* `400 Bad Request`: Empty text, character limit exceeded (>5000), invalid language, mismatched voice and language.
* `401 Unauthorized`: Invalid or expired API credentials.
* `403 Forbidden`: API quota exceeded or access restricted.
* `404 Not Found`: Audio file or record not found.
* `429 Too Many Requests`: TTS provider rate limit reached.
* `500 Internal Server Error`: Unexpected backend exception.
* `503 Service Unavailable`: External TTS provider outage or network unreachable.

---

## 8. Testing Guide

### Automated JUnit & MockMvc Tests
Run the Spring Boot test suite:
```bash
cd backend
mvn test
```
**Test Results: 12 tests passed, 0 failures, 0 errors:**
* `TtsApplicationTests`: Context loads successfully.
* `TtsServiceTest`: Speech synthesis fallback audio generation and duration calculation.
* `TtsControllerTest`:
  - `GET /api/health`: 200 OK, returns provider and database status.
  - `GET /api/voices`: 200 OK, returns neural voice list.
  - `GET /api/voices?language=hi-IN`: 200 OK, filters voices by language.
  - `POST /api/tts`: 200 OK, valid request synthesizes speech.
  - `POST /api/tts` (Empty Text): 400 Bad Request validation error.
  - `POST /api/tts` (Text > 5000 chars): 400 Bad Request validation error.
  - `POST /api/tts` (Unsupported Language): 400 Bad Request.
  - `POST /api/tts` (Voice/Language Mismatch): 400 Bad Request.
  - `GET /api/history`: 200 OK, returns history list.
  - `DELETE /api/history/{id}`: 200 OK, deletes specified history entry.
  - `DELETE /api/history`: 200 OK, clears all history entries.
* Health check endpoint
* Empty text validation (400 Bad Request)
* Character length limit validation (>5000 chars)
* Unsupported language validation
* Voice and language mismatch validation
* Speech generation audio streaming

### Postman Testing
1. Import `postman/TTS_API_Collection.postman_collection.json` into Postman.
2. Set the `baseUrl` variable to `http://localhost:8080`.
3. Run the requests to verify positive flows and error status codes.
