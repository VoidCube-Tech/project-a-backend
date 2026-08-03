ALTER TABLE users
    ALTER COLUMN tenant_id DROP NOT NULL;

UPDATE users
SET tenant_id = NULL
WHERE role = 'ROLE_SUPER_ADMIN';

ALTER TABLE users
    ADD CONSTRAINT chk_users_role_tenant
    CHECK (
        (role = 'ROLE_SUPER_ADMIN' AND tenant_id IS NULL)
        OR
        (role <> 'ROLE_SUPER_ADMIN' AND tenant_id IS NOT NULL)
    );