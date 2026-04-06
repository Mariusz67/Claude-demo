# MemoBee

A full-stack web application for managing memos and reminders with end-to-end encryption, email notifications, and speech-to-text dictation.

## 🚀 Live

- **Frontend**: https://memobee-production.up.railway.app
- **Backend API**: https://pretty-illumination-production.up.railway.app
- **Health Check**: https://pretty-illumination-production.up.railway.app/api/users/health

## 📋 Features

- ✅ RESTful API with Spring Boot
- ✅ PostgreSQL database integration
- ✅ Responsive vanilla JavaScript frontend
- ✅ JWT authentication (24h tokens, HMAC-SHA signed)
- ✅ Role-based access control (admin / user)
- ✅ BCrypt password hashing
- ✅ Client-side AES-256-GCM encryption of memo/reminder text (admins cannot read content)
- ✅ PBKDF2 key derivation (600,000 iterations, SHA-256) from immutable per-user salt
- ✅ Encryption warning with mandatory acknowledgement on registration
- ✅ Self-service password change (old password required, returns new JWT)
- ✅ Self-service password reset via email (15-min expiring tokens)
- ✅ Archive feature — download all decrypted notes as a text file
- ✅ Memos / Reminders CRUD for regular users (Note type removed, merged with Memo)
- ✅ Tile-based type selection UI (memo / reminder)
- ✅ Custom date picker with calendar + hour/minute selectors
- ✅ Email reminders via Resend HTTP API (memobee.eu domain)
- ✅ Scheduled reminder processing (every 5 min, supports repeat intervals with days/hours)
- ✅ HTTP timeout protection (10s connect, 15s request) to prevent scheduler blocking
- ✅ Resilient sending (only marks as sent on success, retries on next tick)
- ✅ Login rate limiting (IP-based lockout after failed attempts)
- ✅ Admin dashboard — read-only user list with created date, note count, last login
- ✅ UTC-aware datetime handling (correct timezone display)
- ✅ XSS protection via HTML escaping
- ✅ Cache-control headers (no stale frontend in browser)
- ✅ Automated HTML quality gates (GitHub Actions)
- ✅ Backend integration & unit tests (38 tests, CI-ready)
- ✅ Continuous deployment to Railway
- ✅ CORS-enabled for cross-origin requests
- ✅ Health check endpoints

## 🏗️ Architecture

```
┌─────────────────┐
│   Frontend      │
│  (HTML/JS/CSS)  │
│   Port: N/A     │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│   Backend API   │
│  Spring Boot    │
│   Port: 8081    │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│   PostgreSQL    │
│   Database      │
│   Port: 5432    │
└─────────────────┘
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.0
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JDBC
- **Build Tool**: Maven

### Frontend
- **Languages**: HTML5, CSS3, JavaScript (ES6+)
- **Styling**: Custom CSS with gradient design
- **HTTP Client**: Fetch API

### DevOps
- **Version Control**: Git + GitHub
- **CI**: GitHub Actions
- **CD**: Railway (manual configuration required)
- **Build System**: Nixpacks

## 📁 Project Structure

```
Memobee/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mariusz/demo/
│   │   │   │   ├── DemoApplication.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   └── NoteController.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Note.java
│   │   │   │   │   └── PasswordResetToken.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── NoteRepository.java
│   │   │   │   │   └── PasswordResetTokenRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── EmailService.java       (Resend HTTP API sending)
│   │   │   │   │   └── ReminderScheduler.java  (5-min reminder processing)
│   │   │   │   └── security/
│   │   │   │       ├── JwtUtil.java
│   │   │   │       ├── JwtFilter.java
│   │   │   │       ├── SecurityConfig.java
│   │   │   │       └── LoginRateLimiter.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── schema.sql
│   │   └── test/
│   │       ├── java/com/mariusz/demo/
│   │       │   ├── SecurityIntegrationTest.java
│   │       │   └── security/
│   │       │       ├── JwtUtilTest.java
│   │       │       └── LoginRateLimiterTest.java
│   │       └── resources/
│   │           └── application.properties  (test-only config, no DB required)
│   ├── pom.xml
│   ├── nixpacks.toml
│   └── railway.json
├── frontend/
│   ├── index.html       (login / self-registration with encryption warning / forgot password)
│   ├── dashboard.html   (admin panel - read-only user list with stats)
│   ├── user.html        (user panel - encrypted memos / reminders / archive / change password)
│   ├── reset.html       (password reset page)
│   └── _headers         (Railway cache-control headers)
├── .github/
│   └── workflows/
│       └── ci.yml
└── README.md
```

## 🔧 Local Development Setup

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd "Memobee/backend"
   ```

2. **Configure local database**

   Create a PostgreSQL database and set environment variables:
   ```bash
   export PGHOST=localhost
   export PGPORT=5432
   export PGDATABASE=demo_db
   export PGUSER=postgres
   export PGPASSWORD=your_password
   export PORT=8081
   ```

3. **Run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Test the backend**
   ```bash
   curl http://localhost:8081/api/users/health
   # Should return: "Backend is running!"
   ```

### Frontend Setup

1. **Open the frontend**
   ```bash
   cd ../frontend
   ```

2. **Serve with a local server** (Python example)
   ```bash
   python -m http.server 8000
   ```

3. **Open in browser**
   ```
   http://localhost:8000
   ```

## 🚢 Railway Deployment (Manual Configuration Required)

### ⚠️ Important: Manual Steps Required

Railway deployment requires manual configuration in the Railway dashboard. This is **not** fully automated CI/CD.

### Backend Service Setup

1. **Create Railway Project**
   - Go to [Railway.app](https://railway.app)
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Connect your repository

2. **Add PostgreSQL Service**
   - Click "+ New" → "Database" → "PostgreSQL"
   - Railway will provision a PostgreSQL instance

3. **Configure Backend Service**

   In the backend service settings:

   **a) Set Root Directory**
   - Settings → Root Directory: `backend`

   **b) Add Environment Variables** (Variables tab)
   ```
   PORT = 8081
   PGHOST = ${{Postgres.PGHOST}}
   PGPORT = ${{Postgres.PGPORT}}
   PGDATABASE = ${{Postgres.PGDATABASE}}
   PGUSER = ${{Postgres.PGUSER}}
   PGPASSWORD = ${{Postgres.PGPASSWORD}}
   JWT_SECRET = <random string, min 32 characters>
   ```

   **c) Configure Networking**
   - Settings → Networking → Public Networking
   - Port: `8081`
   - Click "Generate Domain"

4. **Deploy**
   - Railway will automatically deploy on git push
   - Monitor logs in the "Deployments" tab

### Frontend Service Setup (Optional)

If you want to deploy the frontend separately:

1. **Create New Service**
   - In your Railway project, click "+ New"
   - Select "GitHub Repo"
   - Choose the same repository

2. **Configure Frontend Service**
   - Settings → Root Directory: `frontend`
   - Generate Domain

## 📡 API Endpoints

All endpoints except login and health require `Authorization: Bearer <token>` header.
Admin-only endpoints additionally require the `admin` role in the token.

### Public

```http
GET  /api/users/health
POST /api/users/login              body: { email, password }
POST /api/users/register           body: { name, email, password }
POST /api/users/forgot-password    body: { email }
POST /api/users/reset-password-token  body: { token, newPassword }
```

### Authenticated users

```http
POST /api/users/change-password    body: { email, oldPassword, newPassword }
```

### Admin only

```http
GET    /api/users              list all users (includes noteCount, createdAt, lastLoginAt)
DELETE /api/users/{id}         delete user
```

Admin account is created automatically at startup via `AdminSeeder` when `ADMIN_EMAIL` and `ADMIN_PASSWORD` environment variables are set.

### Notes (authenticated users — ownership enforced)

Users can only access their own notes. Admins can access any user's notes.

```http
GET    /api/notes/user/{email}   list notes for user (own or admin)
POST   /api/notes                create note (userEmail set from JWT, not request body)
PUT    /api/notes/{id}           update note (own or admin)
DELETE /api/notes/{id}           delete note (own or admin)
```

**Note body fields by type** (text field is encrypted client-side before sending):

| Field | Memo | Reminder |
|-------|------|----------|
| `type` | `"memo"` | `"reminder"` |
| `title` | ✅ plaintext | ✅ plaintext — sent in reminder emails |
| `text` | ✅ encrypted | ✅ encrypted — NOT sent by email |
| `frequency` | `never`/`daily`/`weekly`/`monthly`/`quarterly`/`yearly` (default: `never`) | always `"never"` |
| `reminderAt` | — | ISO datetime UTC (e.g. `"2026-03-29T12:00:00"`) |
| `repeatUntilDeleted` | — | `true` (repeat) / `false` (one-time) |
| `repeatDays` | — | integer ≥ 0 (default `0`) |
| `repeatHours` | — | integer ≥ 0 (default `0`) |

## 🧪 Testing

### Backend Tests

Run all 38 backend tests locally:
```bash
cd backend
mvn test
```

Tests use a dedicated `src/test/resources/application.properties` that disables datasource auto-configuration — no PostgreSQL instance required. All repositories are mocked via `@MockitoBean`.

**Test suites:**

| Suite | Type | Tests | Covers |
|-------|------|-------|--------|
| `SecurityIntegrationTest` | Integration (`@SpringBootTest` + MockMvc) | 23 | Public endpoint access, unauthenticated rejection, admin-only authorization, note ownership enforcement, login/registration validation, note input validation |
| `JwtUtilTest` | Unit | 8 | Token generation, claims extraction, expiration, tampered/invalid/wrong-key tokens |
| `LoginRateLimiterTest` | Unit | 7 | IP-based blocking after 5 failures, reset on success, IP isolation, lockout timer |

### Manual API Testing with Postman

1. Import the endpoints above into Postman
2. Test against: `https://pretty-illumination-production.up.railway.app`

### CI Quality Gates (GitHub Actions)

Both jobs run in parallel on every push/PR to `main`:

**Job: `validate-html`**
- ✅ HTML file existence
- ✅ HTML structure (html, head, body tags)
- ✅ Required meta tags (charset, viewport)
- ✅ Title tag presence
- ✅ W3C HTML validation

**Job: `test-backend`**
- ✅ Java 17 + Maven setup
- ✅ Runs all backend tests (`mvn test`)

View workflow: `.github/workflows/ci.yml`

## 🗄️ Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) DEFAULT 'user',
    encryption_salt VARCHAR(255),        -- immutable UUID for client-side encryption key derivation
    created_at TIMESTAMP,                -- UTC account creation time
    last_login_at TIMESTAMP              -- UTC last successful login
);

CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    created_at TEXT,
    type VARCHAR(20) NOT NULL,           -- memo | reminder
    title VARCHAR(255),                  -- plaintext title, used in reminder emails
    text TEXT,                           -- encrypted client-side (ENC:base64iv.base64ciphertext)
    frequency VARCHAR(20) DEFAULT 'never', -- memo: never/daily/weekly/monthly/quarterly/yearly
    attachment_name VARCHAR(255),
    attachment_type VARCHAR(50),
    attachment_data TEXT,                -- Base64 encoded
    last_sent_at TIMESTAMP,              -- set after each email is sent
    -- reminder-specific columns:
    reminder_at TIMESTAMP,               -- UTC datetime to fire the reminder
    repeat_until_deleted BOOLEAN DEFAULT FALSE,
    repeat_days INTEGER DEFAULT 0,
    repeat_hours INTEGER DEFAULT 0,
    repeat_quarters INTEGER DEFAULT 0,   -- legacy, no longer used in UI
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,       -- UTC, 15-minute validity
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);
```

## 🔐 Environment Variables Reference

### Backend Service (Required)

| Variable | Description | Example |
|----------|-------------|---------|
| `PORT` | Application port | `8081` |
| `PGHOST` | PostgreSQL host | `${{Postgres.PGHOST}}` |
| `PGPORT` | PostgreSQL port | `${{Postgres.PGPORT}}` |
| `PGDATABASE` | Database name | `${{Postgres.PGDATABASE}}` |
| `PGUSER` | Database user | `${{Postgres.PGUSER}}` |
| `PGPASSWORD` | Database password | `${{Postgres.PGPASSWORD}}` |
| `JWT_SECRET` | HMAC-SHA signing key (min 32 chars) | `your-random-secret` |
| `ADMIN_EMAIL` | Initial admin account email (optional) | `admin@example.com` |
| `ADMIN_PASSWORD` | Initial admin account password (optional) | `Str0ng!Pass` |
| `RESEND_API_KEY` | Resend HTTP API key for sending emails | `re_...` |
| `MAIL_FROM` | Sender address (verified domain required) | `reminder@memobee.eu` |
| `FRONTEND_URL` | Frontend base URL (for password reset links) | `https://your-frontend.up.railway.app` |

### Frontend Service (None required)

The frontend automatically detects whether it's running locally or on Railway and uses the appropriate backend URL.

## 🚨 Common Issues & Solutions

### Issue: 502 Bad Gateway on Railway

**Cause**: Port mismatch between app and Railway configuration

**Solution**:
1. Ensure `PORT=8081` is set in Railway variables
2. Verify Railway Networking port is set to `8081`
3. Check logs to confirm Tomcat started on port 8081

### Issue: Database Connection Failed

**Cause**: Missing or incorrect database environment variables

**Solution**:
1. Verify all `PG*` variables are set in Railway
2. Ensure they reference the Postgres service: `${{Postgres.PGHOST}}`
3. Check that backend service is linked to Postgres service

### Issue: CORS Errors

**Cause**: Backend not allowing cross-origin requests

**Solution**: Already configured with `@CrossOrigin(origins = "*")` in `UserController.java`

## 📚 Configuration Files Explained

### `backend/nixpacks.toml`
Configures Railway's Nixpacks build system:
- Installs JDK 17 and Maven
- Runs `mvn clean package`
- Starts the JAR file

### `backend/railway.json`
Defines Railway deployment settings:
- Uses Nixpacks builder
- Specifies start command
- Configures restart policy

### `backend/application.properties`
Spring Boot configuration:
- Server port (from `PORT` env var)
- Database connection (from `PG*` env vars)
- Database initialization mode
- Debug logging

### `backend/src/test/resources/application.properties`
Test-only Spring Boot configuration:
- Disables datasource auto-configuration (no PostgreSQL needed)
- Provides dummy JWT secret and mail settings
- Allows tests to run in CI without any environment variables

### `.github/workflows/ci.yml`
GitHub Actions workflow:
- Triggers on push/PR to main
- Job `validate-html`: validates HTML structure + W3C validation
- Job `test-backend`: runs all backend tests with Maven

## 🔐 Encryption

Note text is encrypted client-side before being sent to the server. The database only stores ciphertext — admins cannot read user content.

**How it works:**
1. At registration, a random `encryptionSalt` (UUID) is generated and stored permanently in the database
2. On login, the salt is loaded into `sessionStorage` and used to derive an AES-256-GCM key via PBKDF2 (600,000 iterations, SHA-256)
3. Each note's text is encrypted before save and decrypted after fetch
4. Encrypted text format: `ENC:` prefix + base64(IV) + `.` + base64(ciphertext)
5. The encryption key survives password changes — only account deletion removes it

**Title vs. Text:**
- Each note has a plaintext **title** and an encrypted **text** (body)
- Reminder emails only include the **title** — the encrypted body is never sent by email
- This ensures email notifications are useful while keeping detailed content private

**Limitations:**
- The salt is stored server-side, so a database admin with knowledge of the derivation algorithm could theoretically reconstruct the key
- Note titles are stored in plaintext (visible to admins) — only put sensitive details in the text body

## 🎯 Future Improvements

- [ ] Token blocklist for immediate logout/revocation
- [ ] Automate Railway configuration with Infrastructure as Code (Terraform/Pulumi)
- [x] Add backend unit and integration tests (38 tests)
- [ ] Implement frontend testing (Jest/Cypress)
- [ ] Implement pagination for large datasets
- [ ] Add Docker support for local development
- [ ] Set up staging environment
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Add monitoring and alerting
- [ ] SMS/WhatsApp notifications for reminders (Twilio / WhatsApp Business API)

## 👤 Author

**Mariusz Puto**

## 📄 License

Copyright (c) 2026 Mariusz Puto. All rights reserved.

This source code is published for portfolio and demonstration purposes only.
No part of this repository may be reproduced, copied, modified, distributed,
or used without prior written permission from the author. See [LICENSE](LICENSE) for details.

## 🙏 Acknowledgments

- Built with Claude Sonnet 4.6 / Opus 4.6 assistance
- Deployed on Railway
- CI/CD via GitHub Actions
