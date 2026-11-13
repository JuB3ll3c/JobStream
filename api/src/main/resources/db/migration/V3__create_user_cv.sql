CREATE TABLE user_cv (
    id                BIGSERIAL PRIMARY KEY,
    version           BIGINT    NOT NULL DEFAULT 0,
    created_at        TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    user_id           BIGINT    NOT NULL,
    content           BYTEA     NOT NULL,
    original_filename TEXT      NOT NULL,
    size_bytes        BIGINT    NOT NULL,
    uploaded_at       TIMESTAMP NOT NULL,
    revision          BIGINT    NOT NULL,
    CONSTRAINT fk_user_cv_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
    CONSTRAINT uk_user_cv_user_id UNIQUE (user_id)
);
