CREATE TABLE app_user (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('USER','ADMIN'))
    );

-- ------------------------------------------------------------
-- Table: job
-- ------------------------------------------------------------
CREATE TABLE job (
    id            BIGSERIAL PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP,
    external_id   VARCHAR(255) NOT NULL,
    title         VARCHAR(255) NOT NULL,
    company       VARCHAR(255) NOT NULL,
    location      VARCHAR(255),
    description   TEXT,
    salary_min    INTEGER CHECK (salary_min >= 0),
    salary_max    INTEGER CHECK (salary_max >= 0),
    contract_type VARCHAR(255),
    posted_date   DATE,
    job_url       TEXT,
    requirements  JSONB        NOT NULL DEFAULT '[]'::jsonb,
    user_id       BIGINT       NOT NULL,
    CONSTRAINT fk_job_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE RESTRICT,
    CONSTRAINT uk_job_user_external_id UNIQUE (user_id, external_id)
    );

CREATE INDEX idx_job_user_id ON job(user_id);
CREATE INDEX idx_job_location ON job(location);
CREATE INDEX idx_job_company ON job(company);
