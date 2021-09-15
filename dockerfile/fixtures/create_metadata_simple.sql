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

CREATE TABLE organisation
(
  id INT NOT NULL PRIMARY KEY,
  org_name VARCHAR(255) NOT NULL,
  org_type VARCHAR(20) NOT NULL,
  org_code VARCHAR(15) NOT NULL,
  description VARCHAR,
  org_char CHAR(5)
);
--Use both VARCHAR and CHAR columns. Expect that because there are no CHAR columns other than org_char,
--when org_char is detected as an enumeration, CHAR will be removed from the primitive data types
INSERT INTO organisation(id, org_name, org_type, org_code, description, org_char) VALUES
(1, 'ORG1', 'TYPEA', 'CODEY', 'Description of ORG1', 'CHAR1'),
(2, 'ORG2', 'TYPEA', 'CODEY', 'Description of ORG2', 'CHAR1'),
(3, 'ORG3', 'TYPEA', 'CODEY', 'Description of ORG3', 'CHAR1'),
(4, 'ORG4', 'TYPEA', 'CODEY', 'Description of ORG4', 'CHAR1'),
(5, 'ORG5', 'TYPEB', 'CODEY', 'Description of ORG5', 'CHAR1'),
(6, 'ORG6', 'TYPEB', 'CODEY', 'Description of ORG6', 'CHAR1'),
(7, 'ORG7', 'TYPEB', 'CODEY', 'Description of ORG7', 'CHAR1'),
(8, 'ORG8', 'TYPEB', 'CODEY', 'Description of ORG8', 'CHAR2'),
(9, 'ORG9', 'TYPEB', 'CODEY', 'Description of ORG9', 'CHAR2'),
(10, 'ORG10', 'TYPEA', 'CODEZ', 'Description of ORG10', 'CHAR2'),
(11, 'ORG11', 'TYPEA', 'CODEZ', 'Description of ORG11', 'CHAR2'),
(12, 'ORG12', 'TYPEA', 'CODEZ', 'Description of ORG12', 'CHAR2'),
(13, 'ORG13', 'TYPEA', 'CODEZ', 'Description of ORG13', 'CHAR2'),
(14, 'ORG14', 'TYPEA', 'CODEZ', 'Description of ORG14', 'CHAR2'),
(15, 'ORG15', 'TYPEB', 'CODEZ', 'Description of ORG15', 'CHAR2'),
(16, 'ORG16', 'TYPEB', 'CODEZ', 'Description of ORG16', 'CHAR2'),
(17, 'ORG17', 'TYPEB', 'CODEZ', 'Description of ORG17', 'CHAR2'),
(18, 'ORG18', 'TYPEB', 'CODEZ', 'Description of ORG18', 'CHAR2'),
(19, 'ORG19', 'TYPEB', 'CODEZ', 'Description of ORG19', 'CHAR2'),
(20, 'ORG20', 'TYPEB', 'CODEZ', 'Description of ORG20', 'CHAR2'),
(21, 'ORG21', 'TYPEA', 'CODEX', 'Description of ORG21', 'CHAR3'),
(22, 'ORG22', 'TYPEA', 'CODEX', 'Description of ORG22', 'CHAR3'),
(23, 'ORG23', 'TYPEA', 'CODEX', 'Description of ORG23', 'CHAR3'),
(24, 'ORG24', 'TYPEA', 'CODEX', 'Description of ORG24', 'CHAR3'),
(25, 'ORG25', 'TYPEB', 'CODEX', 'Description of ORG25', 'CHAR3'),
(26, 'ORG26', 'TYPEB', 'CODEX', 'Description of ORG26', 'CHAR3'),
(27, 'ORG27', 'TYPEB', 'CODEX', 'Description of ORG27', 'CHAR3'),
(28, 'ORG28', 'TYPEB', 'CODEX', 'Description of ORG28', 'CHAR3'),
(29, 'ORG29', 'TYPEB', 'CODEX', 'Description of ORG29', 'CHAR3'),
(30, 'ORG30', 'TYPEB', 'CODEX', 'Description of ORG30', 'CHAR3'),
(31, 'ORG31', 'TYPEA', 'CODEX', 'Description of ORG31', 'CHAR3'),
(32, 'ORG32', 'TYPEA', 'CODEX', 'Description of ORG32', 'CHAR3'),
(33, 'ORG33', 'TYPEA', 'CODEX', 'Description of ORG33', 'CHAR3'),
(34, 'ORG34', 'TYPEA', 'CODEX', 'Description of ORG34', 'CHAR3'),
(35, 'ORG35', 'TYPEC', 'CODEX', 'Description of ORG35', 'CHAR3'),
(36, 'ORG36', 'TYPEC', 'CODEX', 'Description of ORG36', 'CHAR3'),
(37, 'ORG37', 'TYPEB', 'CODEX', 'Description of ORG37', 'CHAR3'),
(38, 'ORG38', 'TYPEB', 'CODEX', 'Description of ORG38', 'CHAR3'),
(39, 'ORG39', 'TYPEB', 'CODEX', 'Description of ORG39', 'CHAR3'),
(40, 'ORG40', 'TYPEB', 'CODER', 'Description of ORG40', 'CHAR3');

CREATE TABLE sample
(
  id SERIAL PRIMARY KEY,
  sample_smallint SMALLINT,
  sample_int INTEGER,
  sample_bigint BIGINT,
  sample_decimal DECIMAL(12,3),
  sample_numeric NUMERIC(10,6),
  sample_date DATE,
  sample_timestamp_without_tz TIMESTAMP,
  sample_timestamp_with_tz TIMESTAMP WITH TIME ZONE
);

--sample data: sample_smallint goes from -100 to 100. sample_int goes from 0 to 10000.
--sample_bigint goes from -1000000 to 1000000
WITH RECURSIVE populate AS (
SELECT -100 AS x UNION ALL SELECT x + 1 FROM populate WHERE x < 100
)
INSERT INTO sample (sample_smallint, sample_int, sample_bigint, sample_decimal, sample_numeric, sample_date, sample_timestamp_without_tz, sample_timestamp_with_tz)
SELECT
x, --smallint
x*x, --integer
x*x*x, --bigint
x*x * 573, --decimal
x*x*x / 104756.576, --numeric
TO_DATE('2020-09-01', 'YYYY-MM-DD') + x * INTERVAL '1 day', --date
TO_DATE('2020-09-01', 'YYYY-MM-DD') + x * INTERVAL '1 hour', --timestamp
TO_DATE('2020-09-01', 'YYYY-MM-DD') + x * INTERVAL '1 hour' --timestamp with timezone
FROM populate;

CREATE TABLE bigger_sample (
sample_bigint BIGINT,
sample_decimal DECIMAL(12, 3),
sample_date DATE,
sample_varchar VARCHAR(20)
);
WITH RECURSIVE populate AS (
SELECT 1 AS x UNION ALL SELECT x + 1 FROM populate WHERE x < 500000
)
INSERT INTO bigger_sample (sample_bigint) SELECT x FROM populate;
UPDATE bigger_sample
SET sample_decimal = SIN(sample_bigint),
sample_date = TO_DATE('2020-09-02', 'YYYY-MM-DD') + 200 * SIN(sample_bigint) * INTERVAL '1 hour',
sample_varchar = CONCAT('ENUM'::VARCHAR, TO_CHAR(sample_bigint % 15,'FM99'));