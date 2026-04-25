CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_USER');

INSERT INTO users (id, name, email, password, enabled, created_at)
VALUES
(
    gen_random_uuid(),
    'Admin User',
    'admin@authshield.com',
    '$2a$10$HyDX6sNNKcSej.jKsNz9fe5.CZaWw1FSKBVfpHkiWR/YryuBaZbIG',
    true,
    now()
),
(
    gen_random_uuid(),
    'Normal User',
    'user@authshield.com',
    '$2a$10$GpixfjM3GIhIflSEEZUQA.rYxt79IJjNGXCu1RILurBtb/dfksfQK',
    true,
    now()
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@authshield.com' AND r.name = 'ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'user@authshield.com' AND r.name = 'ROLE_USER';