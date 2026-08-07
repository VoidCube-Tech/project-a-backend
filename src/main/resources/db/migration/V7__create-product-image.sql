CREATE TABLE product_image (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(2048) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    product_id BIGINT NOT NULL REFERENCES product(id)
);

CREATE UNIQUE INDEX ux_product_image_one_main
ON product_image(product_id)
WHERE is_main = TRUE;