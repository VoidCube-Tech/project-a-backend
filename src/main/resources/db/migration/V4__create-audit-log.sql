CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    performed_by_user_id BIGINT REFERENCES users(id),
    tenant_id BIGINT REFERENCES tenant(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);