-- Update existing users to have default password
UPDATE users SET password = '12345' WHERE password IS NULL OR password = '';
