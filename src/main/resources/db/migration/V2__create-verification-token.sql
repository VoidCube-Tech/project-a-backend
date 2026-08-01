CREATE TABLE verification_token (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id)
);