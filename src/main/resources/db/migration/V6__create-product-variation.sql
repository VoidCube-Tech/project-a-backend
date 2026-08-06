CREATE TABLE product_variation(
    id BIGSERIAL PRIMARY KEY,
    variation_name VARCHAR(255) NOT NULL,
    variation_value VARCHAR (255) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    product_id BIGINT NOT NULL REFERENCES product(id),

    CONSTRAINT chk_product_variation_n_negative CHECK (stock_quantity >= 0)

);