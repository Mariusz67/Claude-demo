# Demo SaaS Backend

Spring Boot REST API with PostgreSQL

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (Railway)
- Railway account

## Setup

### 1. Configure Database Connection (Secure Method)

**Create `.env` file** (this file is git-ignored and will NOT be committed):

```bash
cd backend
cp .env.example .env
```

**Edit `.env`** and add your Railway PostgreSQL credentials and JWT secret:

```properties
DATABASE_URL=jdbc:postgresql://YOUR_RAILWAY_HOST:YOUR_PORT/railway
DATABASE_USERNAME=YOUR_USERNAME
DATABASE_PASSWORD=YOUR_PASSWORD
JWT_SECRET=your-random-secret-at-least-32-characters-long
```

**To get Railway PostgreSQL credentials:**
1. Go to Railway dashboard: https://railway.app/dashboard
2. Click on your PostgreSQL service
3. Click "Connect" or "Variables" tab
4. Copy the values:
   - PGHOST and PGPORT → DATABASE_URL
   - PGUSER → DATABASE_USERNAME
   - PGPASSWORD → DATABASE_PASSWORD

**Security Note:**
- ✅ `.env` is in `.gitignore` - your secrets are safe
- ✅ `.env.example` is committed (template without secrets)
- ✅ `application.properties` uses environment variables
- ❌ NEVER commit `.env` to git!
- ❌ Use a strong `JWT_SECRET` (min 32 random characters) — a weak secret allows token forgery

### 2. Install Dependencies

```bash
cd backend
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8081`

### 4. Open the Frontend Locally

Do NOT open `http://localhost:8081` in the browser — that is the backend API only and will return a 404 error.

Instead, open the frontend HTML file directly:
- In VS Code: open `frontend/index.html` → right-click → **Open with Live Server**
- Or double-click `frontend/index.html` in File Explorer — it will open via `file://` in the browser

The frontend JavaScript automatically detects the environment:
- When opened via `localhost` or `file://` → calls `http://localhost:8081` (local backend)
- When opened via Railway domain → calls the production backend URL

**Architecture (local):**
```
Browser (file:// or Live Server)
        ↓ fetch() API calls
Spring Boot on localhost:8081
        ↓ JDBC
PostgreSQL on Railway (cloud)
```

No local PostgreSQL installation needed — the app always connects to Railway's database.

## Authentication

All protected endpoints require a JWT token in the header:
```
Authorization: Bearer <token>
```

Obtain a token via `POST /api/users/login`. Tokens expire after 24 hours.

Two roles exist: `admin` and `user`. Role is embedded in the token and enforced server-side.

## API Endpoints

### Public (no token required)
```
GET  /api/users/health
POST /api/users/login          body: { "email": "...", "password": "..." }
```

### Admin only
```
GET    /api/users
GET    /api/users/{id}
POST   /api/users              body: { name, email, password }
POST   /api/users/admin        body: { name, email, password }  (stricter password rules)
PUT    /api/users/{id}         body: { name, email, password? }
PUT    /api/users/{id}/reset-password   body: { "newPassword": "..." }
DELETE /api/users/{id}
```

### Authenticated users
```
GET    /api/notes/user/{email}
POST   /api/notes              body: { userEmail, type, frequency, text }
PUT    /api/notes/{id}         body: { userEmail, type, frequency, text }
DELETE /api/notes/{id}
```

## Testing with Postman

1. Start the application
2. Login: `POST http://localhost:8081/api/users/login` with `{ "email": "...", "password": "..." }`
3. Copy the `token` from the response
4. Add header `Authorization: Bearer <token>` to all subsequent requests
5. Test health: `GET http://localhost:8081/api/users/health` (no token needed)

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/mariusz/demo/
│   │   │   ├── DemoApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   └── NoteController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   └── Note.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── NoteRepository.java
│   │   │   └── security/
│   │   │       ├── JwtUtil.java        (token generation & validation)
│   │   │       ├── JwtFilter.java      (per-request token check)
│   │   │       └── SecurityConfig.java (route protection rules)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
├── pom.xml
└── README.md
```

## Restarting After Inactivity (Railway)

If the app hasn't been used for a while or the subscription lapsed, follow these steps:

### Railway Services Overview

This project has two Railway services:
- **`pretty-ilumination`** — Spring Boot backend (has PostgreSQL connected)
- **`claude-demo.production`** — Frontend static site

### Step 1: Check Service Status

1. Go to [Railway dashboard](https://railway.app/dashboard)
2. Open your project — you should see both services and PostgreSQL
3. All three should show a green status indicator

### Step 2: Verify PostgreSQL is Connected to Backend

1. Click on the **PostgreSQL** service
2. Go to **Connect** tab → confirm it is linked to `pretty-ilumination` (backend)
3. If not linked, re-add the reference variable in the backend service settings

### Step 3: Check Credentials (if subscription lapsed)

If the subscription expired and Railway recreated PostgreSQL with new credentials:

1. Click on **PostgreSQL** → **Variables** tab
2. Copy: `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`
3. Update your local `.env` file with the new values:

```properties
PGHOST=new_host.railway.app
PGPORT=new_port
PGDATABASE=railway
PGUSER=postgres
PGPASSWORD=new_password
```

### Step 4: Open the Correct URL

- **Frontend (use this to access the app):** open the URL of `claude-demo.production`
  - Railway Dashboard → `claude-demo.production` → **Settings** → **Domains**
- **Backend API only:** `pretty-ilumination` URL — do NOT open this directly in the browser, it only serves `/api/*` endpoints

> **Common mistake:** Opening the backend URL in the browser returns a Spring Boot `Whitelabel Error Page (404)`. This is expected — always use the frontend URL.

### Step 5: Verify Everything Works

1. Open the frontend URL → login page should appear
2. Test the backend health check:
```
GET https://<pretty-ilumination-url>/api/users/health
```

## Technology Stack

- Spring Boot 3.2.1
- Spring Security (JWT, stateless sessions)
- Spring Data JDBC
- PostgreSQL
- Maven
- jjwt (JWT library)
- BCrypt (password hashing)
