# Demo Full-Stack Application

A production-ready full-stack web application demonstrating modern development practices with automated quality gates and cloud deployment.

## 🚀 Live Demo

- **Frontend**: [Your Railway Frontend URL]
- **Backend API**: https://pretty-illumination-production.up.railway.app
- **Health Check**: https://pretty-illumination-production.up.railway.app/api/users/health

## 📋 Features

- ✅ RESTful API with Spring Boot
- ✅ PostgreSQL database integration
- ✅ Responsive vanilla JavaScript frontend
- ✅ JWT authentication (24h tokens, HMAC-SHA signed)
- ✅ Role-based access control (admin / user)
- ✅ BCrypt password hashing
- ✅ Notes CRUD for regular users
- ✅ XSS protection via HTML escaping
- ✅ Cache-control headers (no stale frontend in browser)
- ✅ Automated HTML quality gates (GitHub Actions)
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
- **Framework**: Spring Boot 3.2.1
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
Claude demo/
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
│   │   │   │   │   └── Note.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   └── NoteRepository.java
│   │   │   │   └── security/
│   │   │   │       ├── JwtUtil.java
│   │   │   │       ├── JwtFilter.java
│   │   │   │       └── SecurityConfig.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── schema.sql
│   ├── pom.xml
│   ├── nixpacks.toml
│   └── railway.json
├── frontend/
│   ├── index.html       (login page)
│   ├── dashboard.html   (admin panel - user management)
│   ├── user.html        (user panel - notes)
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
   cd "Claude demo/backend"
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
POST /api/users/login          body: { email, password }
```

### Admin only

```http
GET    /api/users              list all users
GET    /api/users/{id}
POST   /api/users              create user: { name, email, password }
POST   /api/users/admin        create admin user (stricter password rules)
PUT    /api/users/{id}         update user
PUT    /api/users/{id}/reset-password   body: { newPassword }
DELETE /api/users/{id}
```

### Authenticated users

```http
GET    /api/notes/user/{email}   list notes for user
POST   /api/notes                create note: { userEmail, type, frequency, text }
PUT    /api/notes/{id}           update note
DELETE /api/notes/{id}           delete note
```

## 🧪 Testing

### Manual API Testing with Postman

1. Import the endpoints above into Postman
2. Test against: `https://pretty-illumination-production.up.railway.app`

### Automated HTML Quality Gates

GitHub Actions automatically validates:
- ✅ HTML file existence
- ✅ HTML structure (html, head, body tags)
- ✅ Required meta tags (charset, viewport)
- ✅ Title tag presence
- ✅ W3C HTML validation

View workflow: `.github/workflows/ci.yml`

## 🗄️ Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    role VARCHAR(50) DEFAULT 'user'
);

CREATE TABLE notes (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    frequency VARCHAR(50) DEFAULT 'never',
    text TEXT,
    attachment_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

### `.github/workflows/ci.yml`
GitHub Actions workflow:
- Triggers on push/PR to main
- Validates HTML structure
- Runs W3C HTML validator

## 🎯 Future Improvements

- [ ] Add self-registration for users
- [ ] Token blocklist for immediate logout/revocation
- [ ] Automate Railway configuration with Infrastructure as Code (Terraform/Pulumi)
- [ ] Add backend unit and integration tests
- [ ] Implement frontend testing (Jest/Cypress)
- [ ] Implement pagination for large datasets
- [ ] Add Docker support for local development
- [ ] Set up staging environment
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Implement rate limiting
- [ ] Add monitoring and alerting

## 👤 Author

**Mariusz Puto**

## 📄 License

This project is open source and available for educational purposes.

## 🙏 Acknowledgments

- Built with Claude Sonnet 4.5 assistance
- Deployed on Railway
- CI/CD via GitHub Actions
