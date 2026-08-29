ALTER TABLE analytics_event
    DROP CONSTRAINT IF EXISTS
        analytics_event_event_type_check;

ALTER TABLE analytics_event
    DROP CONSTRAINT IF EXISTS
        chk_analytics_event_type;

ALTER TABLE analytics_event
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE analytics_event
    ADD CONSTRAINT chk_analytics_event_type
    CHECK (
        event_type IN (
            'VIEW',
            'ADD_TO_CART',
            'WHATSAPP_CLICK'
        )
    );