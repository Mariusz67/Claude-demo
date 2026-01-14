-- Update existing users to have default password
UPDATE users SET password = '1234' WHERE password IS NULL OR password = '';
