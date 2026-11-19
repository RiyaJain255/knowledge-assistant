-- Application tables. The vector table (vector_store) is created by Spring AI's PgVectorStore.

CREATE TABLE app_user (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE collection (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_collection_user_name ON collection (user_id, lower(name));

CREATE TABLE source_document (
    id            BIGSERIAL PRIMARY KEY,
    collection_id BIGINT       NOT NULL REFERENCES collection (id) ON DELETE CASCADE,
    user_id       BIGINT       NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    filename      VARCHAR(255) NOT NULL,
    file_type     VARCHAR(10)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    storage_path  VARCHAR(500) NOT NULL,
    status        VARCHAR(20)  NOT NULL,      -- PROCESSING | READY | FAILED
    chunk_count   INTEGER      NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_source_document_collection ON source_document (collection_id);
CREATE INDEX idx_source_document_user ON source_document (user_id);
