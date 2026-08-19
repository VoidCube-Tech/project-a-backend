ALTER TABLE analytics_event
    DROP COLUMN entity_name;

ALTER TABLE analytics_event
    DROP COLUMN entity_id;

ALTER TABLE analytics_event
    ADD COLUMN landing_page_id BIGINT NOT NULL;

ALTER TABLE analytics_event
    ADD COLUMN product_id BIGINT NOT NULL;