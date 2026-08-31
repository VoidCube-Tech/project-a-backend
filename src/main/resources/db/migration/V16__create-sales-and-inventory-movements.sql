CREATE TABLE sale (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(15),
    status VARCHAR(30) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    registered_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancelled_by_user_id BIGINT,

    CONSTRAINT fk_sale_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(id),

    CONSTRAINT chk_sale_status
        CHECK (status IN ('CONFIRMED', 'CANCELLED')),

    CONSTRAINT chk_sale_total
        CHECK (total_amount >= 0),

    CONSTRAINT chk_sale_customer_phone
        CHECK (
            customer_phone IS NULL
            OR CHAR_LENGTH(customer_phone) BETWEEN 8 AND 15
        ),

    CONSTRAINT chk_sale_cancellation
        CHECK (
            (
                status = 'CONFIRMED'
                AND cancelled_at IS NULL
                AND cancelled_by_user_id IS NULL
            )
            OR
            (
                status = 'CANCELLED'
                AND cancelled_at IS NOT NULL
                AND cancelled_by_user_id IS NOT NULL
            )
        )
);

CREATE TABLE sale_item (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variation_id BIGINT,
    product_name VARCHAR(255) NOT NULL,
    variation_description VARCHAR(255),
    product_type VARCHAR(30) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(19, 2) NOT NULL,

    CONSTRAINT fk_sale_item_sale
        FOREIGN KEY (sale_id) REFERENCES sale(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_sale_item_product_type
        CHECK (product_type IN ('PHYSICAL', 'DIGITAL')),

    CONSTRAINT chk_sale_item_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_sale_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_sale_item_subtotal
        CHECK (subtotal >= 0)
);

CREATE TABLE inventory_movement (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    variation_id BIGINT,
    sale_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity INTEGER NOT NULL,
    performed_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_movement_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant(id),

    CONSTRAINT fk_inventory_movement_sale
        FOREIGN KEY (sale_id) REFERENCES sale(id),

    CONSTRAINT chk_inventory_movement_type
        CHECK (
            movement_type IN (
                'SALE',
                'SALE_CANCELLATION'
            )
        ),

    CONSTRAINT chk_inventory_movement_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_sale_tenant_created_at
    ON sale (tenant_id, created_at DESC);

CREATE INDEX idx_sale_item_sale
    ON sale_item (sale_id);

CREATE INDEX idx_inventory_movement_tenant_created_at
    ON inventory_movement (tenant_id, created_at DESC);

CREATE INDEX idx_inventory_movement_product_created_at
    ON inventory_movement (product_id, created_at DESC);

CREATE INDEX idx_inventory_movement_sale
    ON inventory_movement (sale_id);