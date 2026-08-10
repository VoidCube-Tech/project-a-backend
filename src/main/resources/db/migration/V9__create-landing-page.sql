CREATE TABLE landing_page(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain_url VARCHAR(255) NOT NULL UNIQUE,
    whatsapp_number VARCHAR(20),
    tenant_id BIGINT NOT NULL REFERENCES tenant(id)
);

CREATE INDEX idx_landing_page_tenant_id
    ON landing_page(tenant_id);