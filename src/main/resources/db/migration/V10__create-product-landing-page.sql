CREATE TABLE product_landing_page(
    landing_page_id BIGINT NOT NULL REFERENCES landing_page(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    PRIMARY KEY (landing_page_id, product_id)
);