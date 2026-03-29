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
