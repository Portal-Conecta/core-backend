CREATE TABLE user_activation_tokens (
                                        id          UUID         NOT NULL,
                                        user_id     UUID         NOT NULL,
                                        token_hash  VARCHAR(255) NOT NULL,
                                        expires_at  TIMESTAMPTZ  NOT NULL,
                                        used_at     TIMESTAMPTZ,
                                        created_at  TIMESTAMPTZ  NOT NULL,

                                        CONSTRAINT pk_user_activation_tokens PRIMARY KEY (id),
                                        CONSTRAINT uk_user_activation_tokens_token_hash UNIQUE (token_hash),
                                        CONSTRAINT fk_user_activation_tokens_user_id FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_user_activation_tokens_user_id
    ON user_activation_tokens (user_id);

CREATE INDEX idx_user_activation_tokens_expires_at
    ON user_activation_tokens (expires_at);
