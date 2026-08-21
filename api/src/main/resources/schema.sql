CREATE TABLE IF NOT EXISTS app_user (
    id          BIGSERIAL PRIMARY KEY,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('USER','ADMIN'))
    );

-- ------------------------------------------------------------
-- Table: job
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job (
    id            BIGSERIAL PRIMARY KEY,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    external_id   VARCHAR(255) NOT NULL UNIQUE,
    title         VARCHAR(255) NOT NULL,
    company       VARCHAR(255) NOT NULL,
    location      VARCHAR(255),
    description   TEXT,
    salary_min    INTEGER CHECK (salary_min >= 0),
    salary_max    INTEGER CHECK (salary_max >= 0),
    contract_type VARCHAR(50),
    posted_date   DATE,
    job_url       TEXT,
    requirements  JSONB        NOT NULL DEFAULT '[]'::jsonb,
    user_id       BIGINT       REFERENCES app_user(id) ON DELETE SET NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_job_external_id ON job(external_id);
CREATE INDEX IF NOT EXISTS idx_job_user_id ON job(user_id);
CREATE INDEX IF NOT EXISTS idx_job_location ON job(location);
CREATE INDEX IF NOT EXISTS idx_job_company ON job(company);