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

-- Add last_sent_at column for reminder scheduling (safe to run multiple times)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS last_sent_at TIMESTAMP;

-- Add reminder-specific columns
ALTER TABLE notes ADD COLUMN IF NOT EXISTS reminder_at TIMESTAMP;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS repeat_until_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS repeat_days INTEGER DEFAULT 0;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS repeat_hours INTEGER DEFAULT 0;
ALTER TABLE notes ADD COLUMN IF NOT EXISTS repeat_quarters INTEGER DEFAULT 0;

-- Encryption salt for E2E encryption (immutable per user)
ALTER TABLE users ADD COLUMN IF NOT EXISTS encryption_salt VARCHAR(255);

-- Note title (plaintext, used in reminder emails instead of encrypted body)
-- Note: in PostgreSQL column order cannot be changed after creation;
-- logically title sits between type and text.
ALTER TABLE notes ADD COLUMN IF NOT EXISTS title VARCHAR(255);

-- User activity tracking
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);
