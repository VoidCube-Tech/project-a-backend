CREATE TABLE plan (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE plan_features (
    plan_id BIGINT NOT NULL REFERENCES plan(id) ON DELETE CASCADE,
    feature_order INTEGER NOT NULL,
    feature VARCHAR(255) NOT NULL,
    PRIMARY KEY (plan_id, feature_order)
);

INSERT INTO plan (name)
VALUES
    ('Basic'),
    ('Plus'),
    ('Premium');

INSERT INTO plan_features (
    plan_id,
    feature_order,
    feature
)
SELECT
    plan.id,
    template.feature_order,
    template.feature
FROM plan
JOIN (
    VALUES
        ('Basic', 0, 'Atendimento por e-mail'),
        ('Basic', 1, 'Suporte em horário comercial'),

        ('Plus', 0, 'Atendimento prioritário'),
        ('Plus', 1, 'Suporte por e-mail e WhatsApp'),

        ('Premium', 0, 'Atendimento prioritário'),
        ('Premium', 1, 'Suporte por e-mail e WhatsApp'),
        ('Premium', 2, 'Acompanhamento especializado')
) AS template(plan_name, feature_order, feature)
    ON template.plan_name = plan.name;

CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES plan(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    email_verified_at TIMESTAMP,
    tenant_id BIGINT NOT NULL UNIQUE REFERENCES tenant(id)
);
