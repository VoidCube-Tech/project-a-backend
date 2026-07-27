CREATE TABLE plan (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    features TEXT[]
);

CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    plan_id BIGINT REFERENCES plan(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email_verified_at TIMESTAMP,
    tenant_id BIGINT NOT NULL UNIQUE REFERENCES tenant(id)
);
