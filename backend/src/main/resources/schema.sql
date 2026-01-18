-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL DEFAULT '1234',
    role VARCHAR(20) NOT NULL DEFAULT 'user'
);

-- Create notes table
CREATE TABLE IF NOT EXISTS notes (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    created_at TEXT,
    type VARCHAR(20) NOT NULL,
    text TEXT,
    frequency VARCHAR(20) DEFAULT 'never',
    attachment_name VARCHAR(255),
    attachment_type VARCHAR(50),
    attachment_data TEXT,
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);
