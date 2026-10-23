-- Insert a default user (Password: password)
INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}password', true);
-- Assign a role
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');