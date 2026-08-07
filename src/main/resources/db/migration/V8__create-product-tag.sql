CREATE TABLE product_tag(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tenant_id BIGINT NOT NULL REFERENCES tenant(id)
);

CREATE UNIQUE INDEX ux_produc_tag_tenant_name
ON product_tag(tenant_id, LOWER(name));

CREATE TABLE product_tag_association (
    product_id BIGINT NOT NULL REFERENCES product(id),
    tag_id BIGINT NOT NULL REFERENCES product_tag(id)

    PRIMARY KEY(product_id, tag_id)
);