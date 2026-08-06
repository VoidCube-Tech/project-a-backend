CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(9, 2) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    description TEXT,
    stock_quantity INTEGER,
    deleted_at TIMESTAMP,
    tenant_id BIGINT NOT NULL REFERENCES tenant(id),

    CONSTRAINT chk_product_type CHECK(product_type IN ('PHYSICAL', 'DIGITAL')),

    CONSTRAINT chk_product_stock_non_negatice CHECK (product_Type <> 'PHYSICAL' OR stock_quantity IS NOT NULL),

    CONSTRAINT chk_product_price_non_negative CHECK (price >= 0)



)