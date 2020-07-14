CREATE DATABASE metadata_simple OWNER maurodatamapper;

\c metadata_simple;

CREATE TABLE IF NOT EXISTS catalogue_item (
    id            UUID         NOT NULL
        CONSTRAINT catalogue_item_pkey
            PRIMARY KEY,
    version       BIGINT       NOT NULL,
    date_created  TIMESTAMP    NOT NULL,
    domain_type   VARCHAR(255) NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    path          TEXT         NOT NULL,
    depth         INTEGER      NOT NULL,
    created_by_id UUID         NOT NULL,
    label         TEXT         NOT NULL,
    description   TEXT
);

CREATE INDEX IF NOT EXISTS catalogue_item_domain_type_index
    ON catalogue_item(domain_type);

CREATE INDEX IF NOT EXISTS catalogue_item_created_by_idx
    ON catalogue_item(created_by_id);

CREATE TABLE IF NOT EXISTS catalogue_user (
    id               UUID         NOT NULL
        CONSTRAINT catalogue_user_pkey
            PRIMARY KEY,
    version          BIGINT       NOT NULL,
    salt             BYTEA        NOT NULL,
    date_created     TIMESTAMP    NOT NULL,
    first_name       VARCHAR(255) NOT NULL,
    domain_type      VARCHAR(255) NOT NULL,
    last_updated     TIMESTAMP    NOT NULL,
    organisation     VARCHAR(255),
    user_role        VARCHAR(255) NOT NULL,
    job_title        VARCHAR(255),
    email_address    VARCHAR(255) NOT NULL
        CONSTRAINT uk_26qjnuqu76954q376opkqelqd
            UNIQUE,
    user_preferences VARCHAR(255),
    password         BYTEA,
    created_by_id    UUID
        CONSTRAINT fk3s09b1t9lwqursuetowl2bi9t
            REFERENCES catalogue_user,
    temp_password    VARCHAR(255),
    last_name        VARCHAR(255) NOT NULL,
    last_login       TIMESTAMP,
    disabled         BOOLEAN
);

CREATE INDEX IF NOT EXISTS catalogue_user_created_by_idx
    ON catalogue_user(created_by_id);

ALTER TABLE catalogue_item
    ADD CONSTRAINT fkf9kx3d90ixy5pqc1d6kqgjui7
        FOREIGN KEY (created_by_id) REFERENCES catalogue_user;

CREATE TABLE IF NOT EXISTS metadata (
    id                UUID         NOT NULL
        CONSTRAINT metadata_pkey
            PRIMARY KEY,
    version           BIGINT       NOT NULL,
    date_created      TIMESTAMP    NOT NULL,
    domain_type       VARCHAR(255) NOT NULL,
    catalogue_item_id UUID         NOT NULL
        CONSTRAINT fkk26px3s00mg783vb5gomhsw07
            REFERENCES catalogue_item,
    last_updated      TIMESTAMP    NOT NULL,
    namespace         TEXT         NOT NULL,
    value             TEXT         NOT NULL,
    created_by_id     UUID         NOT NULL
        CONSTRAINT fkfo9b0grugrero8q84mxjst7jr
            REFERENCES catalogue_user,
    key               TEXT         NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS unique_item_id_namespace_key
    ON metadata(catalogue_item_id, namespace, key);

CREATE INDEX IF NOT EXISTS metadata_catalogue_item_idx
    ON metadata(catalogue_item_id);

CREATE INDEX IF NOT EXISTS metadata_created_by_idx
    ON metadata(created_by_id);

