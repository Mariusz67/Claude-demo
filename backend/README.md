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

**Edit `.env`** and add your Railway PostgreSQL credentials:

```properties
DATABASE_URL=jdbc:postgresql://YOUR_RAILWAY_HOST:YOUR_PORT/railway
DATABASE_USERNAME=YOUR_USERNAME
DATABASE_PASSWORD=YOUR_PASSWORD
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

## API Endpoints

### Health Check
```
GET /api/users/health
```

### User Management

**Get all users:**
```
GET /api/users
```

**Get user by ID:**
```
GET /api/users/{id}
```

**Create new user:**
```
POST /api/users
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Update user:**
```
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```

**Delete user:**
```
DELETE /api/users/{id}
```

## Testing with Postman

1. Start the application
2. Open Postman
3. Test health endpoint: `GET http://localhost:8081/api/users/health`
4. Create a user: `POST http://localhost:8081/api/users` with JSON body
5. Get all users: `GET http://localhost:8081/api/users`

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/mariusz/demo/
│   │   │   ├── DemoApplication.java
│   │   │   ├── controller/
│   │   │   │   └── UserController.java
│   │   │   ├── model/
│   │   │   │   └── User.java
│   │   │   └── repository/
│   │   │       └── UserRepository.java
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
- Spring Data JDBC
- PostgreSQL
- Maven
