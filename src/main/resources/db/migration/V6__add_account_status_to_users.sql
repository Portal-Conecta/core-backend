ALTER TABLE users
    ADD COLUMN account_status VARCHAR(32);

UPDATE users
SET account_status = CASE
    WHEN deleted_at IS NOT NULL THEN 'PENDING_DELETION'
    WHEN active = TRUE THEN 'ACTIVE'
    ELSE 'PENDING_ACTIVATION'
END;

ALTER TABLE users
    ALTER COLUMN account_status SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT ck_users_account_status
        CHECK (account_status IN ('PENDING_ACTIVATION', 'ACTIVE', 'DISABLED', 'PENDING_DELETION'));

CREATE INDEX idx_users_account_status
    ON users (account_status);
