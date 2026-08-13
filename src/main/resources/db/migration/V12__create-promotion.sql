CREATE TABLE promotion(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    promotion_type VARCHAR(50) NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenant(id),
    discount_percentage NUMERIC(5, 2),
    
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    scheduled_discount_value NUMERIC(19, 2),

    coupon_code VARCHAR(100),
    coupon_discount_value NUMERIC(19, 2),
    usage_limit INTEGER,

    CONSTRAINT chk_promotion_rules CHECK(
        (
            promotion_type = 'PERCENTAGE'
            AND discount_percentage IS NOT NULL
            AND discount_percentage > 0
            AND discount_percentage <= 100
            AND start_date IS NULL
            AND end_date IS NULL
            AND scheduled_discount_value IS NULL
            AND coupon_code IS NULL
            AND coupon_discount_value IS NULL
            AND usage_limit IS NULL
        )

        OR

        (
            promotion_type = 'SCHEDULED'
            AND discount_percentage IS NULL
            AND start_date IS NOT NULL
            AND end_date IS NOT NULL
            AND end_date > start_date
            AND scheduled_discount_value IS NOT NULL
            AND scheduled_discount_value > 0
            AND coupon_code IS NULL
            AND coupon_discount_value IS NULL
            AND usage_limit IS NULL

        )

        OR

        (
            promotion_type = 'COUPON'
            AND discount_percentage IS NULL
            AND start_date IS NULL
            AND end_date IS NULL
            AND scheduled_discount_value IS NULL
            AND coupon_code IS NOT NULL
            AND BTRIM(coupon_code) <> ''
            AND coupon_discount_value IS NOT NULL
            AND coupon_discount_value > 0
            AND usage_limit IS NOT NULL
            AND usage_limit > 0
        )
    )
);

CREATE INDEX idx_promotion_type
ON promotion(promotion_type);

CREATE INDEX idx_promotion_tenant_id
ON promotion(tenant_id);

CREATE UNIQUE INDEX ux_promotion_tenant_coupon_code
ON promotion(tenant_id, LOWER(BTRIM(coupon_code)))
WHERE coupon_code IS NOT NULL;

CREATE TABLE product_promotion (
    promotion_id BIGINT NOT NULL REFERENCES promotion(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    PRIMARY KEY(promotion_id, product_id)
);

CREATE INDEX idx_product_promotion_product_id
ON product_promotion(product_id);
