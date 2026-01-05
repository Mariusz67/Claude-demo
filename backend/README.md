# Demo SaaS Backend

Spring Boot REST API with PostgreSQL

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (Railway)

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

The application will start on `http://localhost:8080`

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
3. Test health endpoint: `GET http://localhost:8080/api/users/health`
4. Create a user: `POST http://localhost:8080/api/users` with JSON body
5. Get all users: `GET http://localhost:8080/api/users`

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

## Technology Stack

- Spring Boot 3.2.1
- Spring Data JDBC
- PostgreSQL
- Maven
