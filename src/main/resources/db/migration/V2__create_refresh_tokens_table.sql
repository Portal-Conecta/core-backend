CREATE TABLE refresh_tokens (
                                id         UUID        NOT NULL,
                                token      TEXT        NOT NULL,
                                expires_at TIMESTAMPTZ NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL,

                                CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
                                CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);