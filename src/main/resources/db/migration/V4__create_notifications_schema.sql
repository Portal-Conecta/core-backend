CREATE TABLE notifications (
                               id              UUID          NOT NULL,
                               message_id      VARCHAR(255)  NOT NULL,
                               correlation_id  VARCHAR(255),
                               source          VARCHAR(255)  NOT NULL,
                               event_type      VARCHAR(255)  NOT NULL,
                               occurred_at     TIMESTAMPTZ   NOT NULL,
                               title           VARCHAR(255)  NOT NULL,
                               body            TEXT          NOT NULL,
                               metadata        TEXT,
                               created_at      TIMESTAMPTZ   NOT NULL,

                               CONSTRAINT pk_notifications PRIMARY KEY (id),
                               CONSTRAINT uk_notifications_message_id UNIQUE (message_id)
);

CREATE TABLE user_notifications (
                                    id               UUID         NOT NULL,
                                    notification_id  UUID         NOT NULL,
                                    user_id          UUID         NOT NULL,
                                    read_at          TIMESTAMPTZ,
                                    dismissed_at     TIMESTAMPTZ,
                                    created_at       TIMESTAMPTZ  NOT NULL,

                                    CONSTRAINT pk_user_notifications PRIMARY KEY (id),
                                    CONSTRAINT uk_user_notifications_notification_id_user_id UNIQUE (notification_id, user_id),
                                    CONSTRAINT fk_user_notifications_notification_id FOREIGN KEY (notification_id) REFERENCES notifications (id),
                                    CONSTRAINT fk_user_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_notifications_user_id
    ON user_notifications (user_id);

CREATE INDEX idx_user_notifications_created_at
    ON user_notifications (user_id, created_at);