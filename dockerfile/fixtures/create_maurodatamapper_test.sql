CREATE DATABASE maurodatamapper_test OWNER maurodatamapper;

\c maurodatamapper_test;

CREATE SCHEMA core;

ALTER SCHEMA core OWNER TO maurodatamapper;

CREATE SCHEMA datamodel;

ALTER SCHEMA datamodel OWNER TO maurodatamapper;

CREATE TABLE core.annotation (
    id                         UUID         NOT NULL
        CONSTRAINT annotation_pkey
            PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    path                       TEXT         NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    depth                      INTEGER      NOT NULL,
    catalogue_item_id          UUID,
    parent_annotation_id       UUID
        CONSTRAINT fknrnwt8d2s4kytg7mis2rg2a5x
            REFERENCES core.annotation,
    created_by                 VARCHAR(255) NOT NULL,
    label                      TEXT         NOT NULL,
    description                TEXT,
    child_annotations_idx      INTEGER
);

ALTER TABLE core.annotation
    OWNER TO maurodatamapper;

CREATE INDEX annotation_parent_annotation_idx
    ON core.annotation(parent_annotation_id);

CREATE INDEX annotation_created_by_idx
    ON core.annotation(created_by);

CREATE TABLE core.api_property (
    id              UUID         NOT NULL
        CONSTRAINT api_property_pkey
            PRIMARY KEY,
    version         BIGINT       NOT NULL,
    last_updated_by VARCHAR(255) NOT NULL,
    date_created    TIMESTAMP    NOT NULL,
    last_updated    TIMESTAMP    NOT NULL,
    value           TEXT         NOT NULL,
    created_by      VARCHAR(255) NOT NULL,
    key             VARCHAR(255) NOT NULL
);

ALTER TABLE core.api_property
    OWNER TO maurodatamapper;

CREATE INDEX apiproperty_created_by_idx
    ON core.api_property(created_by);

CREATE TABLE core.breadcrumb_tree (
    id                  UUID         NOT NULL
        CONSTRAINT breadcrumb_tree_pkey
            PRIMARY KEY,
    version             BIGINT       NOT NULL,
    domain_type         VARCHAR(255) NOT NULL,
    finalised           BOOLEAN,
    domain_id           UUID,
    tree_string         TEXT         NOT NULL,
    top_breadcrumb_tree BOOLEAN      NOT NULL,
    label               VARCHAR(255),
    parent_id           UUID
        CONSTRAINT fk1hraqwgiiva4reb2v6do4it81
            REFERENCES core.breadcrumb_tree
);

ALTER TABLE core.breadcrumb_tree
    OWNER TO maurodatamapper;

CREATE TABLE core.classifier (
    id                              UUID         NOT NULL
        CONSTRAINT classifier_pkey
            PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    path                            TEXT         NOT NULL,
    depth                           INTEGER      NOT NULL,
    parent_classifier_id            UUID
        CONSTRAINT fkahkm58kcer6a9q2v01ealovr6
            REFERENCES core.classifier,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    created_by                      VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    label                           TEXT         NOT NULL
        CONSTRAINT uk_j7bbt97ko557eewc3u50ha8ko
            UNIQUE,
    description                     TEXT
);

ALTER TABLE core.classifier
    OWNER TO maurodatamapper;

CREATE INDEX classifier_parent_classifier_idx
    ON core.classifier(parent_classifier_id);

CREATE INDEX classifier_created_by_idx
    ON core.classifier(created_by);

CREATE TABLE core.edit (
    id                   UUID         NOT NULL
        CONSTRAINT edit_pkey
            PRIMARY KEY,
    version              BIGINT       NOT NULL,
    date_created         TIMESTAMP    NOT NULL,
    last_updated         TIMESTAMP    NOT NULL,
    resource_domain_type VARCHAR(255) NOT NULL,
    resource_id          UUID         NOT NULL,
    created_by           VARCHAR(255) NOT NULL,
    description          VARCHAR(255) NOT NULL
);

ALTER TABLE core.edit
    OWNER TO maurodatamapper;

CREATE INDEX edit_created_by_idx
    ON core.edit(created_by);

CREATE TABLE core.email (
    id                    UUID         NOT NULL
        CONSTRAINT email_pkey
            PRIMARY KEY,
    version               BIGINT       NOT NULL,
    sent_to_email_address VARCHAR(255) NOT NULL,
    successfully_sent     BOOLEAN      NOT NULL,
    body                  TEXT         NOT NULL,
    date_time_sent        TIMESTAMP    NOT NULL,
    email_service_used    VARCHAR(255) NOT NULL,
    failure_reason        TEXT,
    subject               TEXT         NOT NULL
);

ALTER TABLE core.email
    OWNER TO maurodatamapper;

CREATE TABLE core.folder (
    id                              UUID         NOT NULL
        CONSTRAINT folder_pkey
            PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    path                            TEXT         NOT NULL,
    deleted                         BOOLEAN      NOT NULL,
    depth                           INTEGER      NOT NULL,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    parent_folder_id                UUID
        CONSTRAINT fk57g7veis1gp5wn3g0mp0x57pl
            REFERENCES core.folder,
    created_by                      VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    label                           TEXT         NOT NULL,
    description                     TEXT
);

ALTER TABLE core.folder
    OWNER TO maurodatamapper;

CREATE INDEX folder_parent_folder_idx
    ON core.folder(parent_folder_id);

CREATE INDEX folder_created_by_idx
    ON core.folder(created_by);

CREATE TABLE core.metadata (
    id                         UUID         NOT NULL
        CONSTRAINT metadata_pkey
            PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    namespace                  TEXT         NOT NULL,
    catalogue_item_id          UUID,
    value                      TEXT         NOT NULL,
    created_by                 VARCHAR(255) NOT NULL,
    key                        TEXT         NOT NULL
);

ALTER TABLE core.metadata
    OWNER TO maurodatamapper;

CREATE INDEX metadata_catalogue_item_idx
    ON core.metadata(catalogue_item_id);

CREATE INDEX metadata_created_by_idx
    ON core.metadata(created_by);

CREATE TABLE core.reference_file (
    id                         UUID         NOT NULL
        CONSTRAINT reference_file_pkey
            PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    file_size                  BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    file_type                  VARCHAR(255) NOT NULL,
    file_name                  VARCHAR(255) NOT NULL,
    file_contents              BYTEA        NOT NULL,
    catalogue_item_id          UUID,
    created_by                 VARCHAR(255) NOT NULL
);

ALTER TABLE core.reference_file
    OWNER TO maurodatamapper;

CREATE INDEX referencefile_created_by_idx
    ON core.reference_file(created_by);

CREATE TABLE core.semantic_link (
    id                                UUID         NOT NULL
        CONSTRAINT semantic_link_pkey
            PRIMARY KEY,
    version                           BIGINT       NOT NULL,
    date_created                      TIMESTAMP    NOT NULL,
    target_catalogue_item_id          UUID         NOT NULL,
    last_updated                      TIMESTAMP    NOT NULL,
    catalogue_item_domain_type        VARCHAR(255) NOT NULL,
    target_catalogue_item_domain_type VARCHAR(255) NOT NULL,
    link_type                         VARCHAR(255) NOT NULL,
    catalogue_item_id                 UUID,
    created_by                        VARCHAR(255) NOT NULL
);

ALTER TABLE core.semantic_link
    OWNER TO maurodatamapper;

CREATE INDEX semantic_link_target_catalogue_item_idx
    ON core.semantic_link(target_catalogue_item_id);

CREATE INDEX semantic_link_catalogue_item_idx
    ON core.semantic_link(catalogue_item_id);

CREATE INDEX semanticlink_created_by_idx
    ON core.semantic_link(created_by);

CREATE TABLE core.user_image_file (
    id            UUID         NOT NULL
        CONSTRAINT user_image_file_pkey
            PRIMARY KEY,
    version       BIGINT       NOT NULL,
    file_size     BIGINT       NOT NULL,
    date_created  TIMESTAMP    NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    file_type     VARCHAR(255) NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    user_id       UUID         NOT NULL,
    file_contents BYTEA        NOT NULL,
    created_by    VARCHAR(255) NOT NULL
);

ALTER TABLE core.user_image_file
    OWNER TO maurodatamapper;

CREATE INDEX userimagefile_created_by_idx
    ON core.user_image_file(created_by);

CREATE TABLE core.version_link (
    id                         UUID         NOT NULL
        CONSTRAINT version_link_pkey
            PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    target_model_domain_type   VARCHAR(255) NOT NULL,
    link_type                  VARCHAR(255) NOT NULL,
    target_model_id            UUID         NOT NULL,
    catalogue_item_id          UUID,
    created_by                 VARCHAR(255) NOT NULL
);

ALTER TABLE core.version_link
    OWNER TO maurodatamapper;

CREATE INDEX version_link_target_model_idx
    ON core.version_link(target_model_id);

CREATE INDEX version_link_catalogue_item_idx
    ON core.version_link(catalogue_item_id);

CREATE INDEX versionlink_created_by_idx
    ON core.version_link(created_by);

CREATE TABLE datamodel.data_model (
    id                              UUID         NOT NULL
        CONSTRAINT data_model_pkey
            PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    finalised                       BOOLEAN      NOT NULL,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    date_finalised                  TIMESTAMP,
    documentation_version           VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    model_type                      VARCHAR(255) NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    organisation                    VARCHAR(255),
    deleted                         BOOLEAN      NOT NULL,
    author                          VARCHAR(255),
    breadcrumb_tree_id              UUID         NOT NULL
        CONSTRAINT fk9ybmrposbekl2h5pnwet4fx30
            REFERENCES core.breadcrumb_tree,
    folder_id                       UUID         NOT NULL
        CONSTRAINT fk5vqrag93xcmptnduomuj1d5up
            REFERENCES core.folder,
    created_by                      VARCHAR(255) NOT NULL,
    aliases_string                  TEXT,
    label                           TEXT         NOT NULL,
    description                     TEXT
);

ALTER TABLE datamodel.data_model
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.data_class (
    id                   UUID         NOT NULL
        CONSTRAINT data_class_pkey
            PRIMARY KEY,
    version              BIGINT       NOT NULL,
    date_created         TIMESTAMP    NOT NULL,
    last_updated         TIMESTAMP    NOT NULL,
    path                 TEXT         NOT NULL,
    depth                INTEGER      NOT NULL,
    min_multiplicity     INTEGER,
    max_multiplicity     INTEGER,
    parent_data_class_id UUID
        CONSTRAINT fk71lrhqamsxh1b57sbigrgonq2
            REFERENCES datamodel.data_class,
    breadcrumb_tree_id   UUID         NOT NULL
        CONSTRAINT fk4yr99q0xt49n31x48e78do1rq
            REFERENCES core.breadcrumb_tree,
    data_model_id        UUID         NOT NULL
        CONSTRAINT fk27usn28pto0b239mwltrfmksg
            REFERENCES datamodel.data_model,
    idx                  INTEGER      NOT NULL,
    created_by           VARCHAR(255) NOT NULL,
    aliases_string       TEXT,
    label                TEXT         NOT NULL,
    description          TEXT
);

ALTER TABLE datamodel.data_class
    OWNER TO maurodatamapper;

CREATE INDEX data_class_parent_data_class_idx
    ON datamodel.data_class(parent_data_class_id);

CREATE INDEX data_class_data_model_idx
    ON datamodel.data_class(data_model_id);

CREATE INDEX dataclass_created_by_idx
    ON datamodel.data_class(created_by);

CREATE INDEX datamodel_created_by_idx
    ON datamodel.data_model(created_by);

CREATE TABLE datamodel.data_type (
    id                 UUID         NOT NULL
        CONSTRAINT data_type_pkey
            PRIMARY KEY,
    version            BIGINT       NOT NULL,
    date_created       TIMESTAMP    NOT NULL,
    domain_type        VARCHAR(15)  NOT NULL,
    last_updated       TIMESTAMP    NOT NULL,
    path               TEXT         NOT NULL,
    depth              INTEGER      NOT NULL,
    breadcrumb_tree_id UUID         NOT NULL
        CONSTRAINT fksiu83nftgdvb7kdvaik9fghsj
            REFERENCES core.breadcrumb_tree,
    data_model_id      UUID         NOT NULL
        CONSTRAINT fkbqs2sknmwe6i3rtwrhflk9s5n
            REFERENCES datamodel.data_model,
    idx                INTEGER      NOT NULL,
    created_by         VARCHAR(255) NOT NULL,
    aliases_string     TEXT,
    label              TEXT         NOT NULL,
    description        TEXT,
    class              VARCHAR(255) NOT NULL,
    units              VARCHAR(255),
    reference_class_id UUID
        CONSTRAINT fkribr7hv9shypnj2iru0hsx2sn
            REFERENCES datamodel.data_class
);

ALTER TABLE datamodel.data_type
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.data_element (
    id                 UUID         NOT NULL
        CONSTRAINT data_element_pkey
            PRIMARY KEY,
    version            BIGINT       NOT NULL,
    date_created       TIMESTAMP    NOT NULL,
    data_class_id      UUID         NOT NULL
        CONSTRAINT fk86to96ckvjf64qlwvosltcnsm
            REFERENCES datamodel.data_class,
    last_updated       TIMESTAMP    NOT NULL,
    path               TEXT         NOT NULL,
    depth              INTEGER      NOT NULL,
    min_multiplicity   INTEGER,
    max_multiplicity   INTEGER,
    breadcrumb_tree_id UUID         NOT NULL
        CONSTRAINT fk6e7wo4o9bw27vk32roeo91cyn
            REFERENCES core.breadcrumb_tree,
    data_type_id       UUID         NOT NULL
        CONSTRAINT fkncb91jl5cylo6nmoolmkif0y4
            REFERENCES datamodel.data_type,
    idx                INTEGER      NOT NULL,
    created_by         VARCHAR(255) NOT NULL,
    aliases_string     TEXT,
    label              TEXT         NOT NULL,
    description        TEXT
);

ALTER TABLE datamodel.data_element
    OWNER TO maurodatamapper;

CREATE INDEX data_element_data_class_idx
    ON datamodel.data_element(data_class_id);

CREATE INDEX data_element_data_type_idx
    ON datamodel.data_element(data_type_id);

CREATE INDEX dataelement_created_by_idx
    ON datamodel.data_element(created_by);

CREATE INDEX data_type_data_model_idx
    ON datamodel.data_type(data_model_id);

CREATE INDEX datatype_created_by_idx
    ON datamodel.data_type(created_by);

CREATE INDEX reference_type_reference_class_idx
    ON datamodel.data_type(reference_class_id);

CREATE TABLE datamodel.enumeration_value (
    id                  UUID         NOT NULL
        CONSTRAINT enumeration_value_pkey
            PRIMARY KEY,
    version             BIGINT       NOT NULL,
    date_created        TIMESTAMP    NOT NULL,
    enumeration_type_id UUID         NOT NULL
        CONSTRAINT fkam3sx31p5a0eap02h4iu1nwsg
            REFERENCES datamodel.data_type,
    value               TEXT         NOT NULL,
    last_updated        TIMESTAMP    NOT NULL,
    path                TEXT         NOT NULL,
    depth               INTEGER      NOT NULL,
    breadcrumb_tree_id  UUID         NOT NULL
        CONSTRAINT fkj6s22vawbgx8qbi6u95umov5t
            REFERENCES core.breadcrumb_tree,
    idx                 INTEGER      NOT NULL,
    category            TEXT,
    created_by          VARCHAR(255) NOT NULL,
    aliases_string      TEXT,
    key                 TEXT         NOT NULL,
    label               TEXT         NOT NULL,
    description         TEXT
);

ALTER TABLE datamodel.enumeration_value
    OWNER TO maurodatamapper;

CREATE INDEX enumeration_value_enumeration_type_idx
    ON datamodel.enumeration_value(enumeration_type_id);

CREATE INDEX enumerationvalue_created_by_idx
    ON datamodel.enumeration_value(created_by);

CREATE TABLE datamodel.join_enumerationvalue_to_facet (
    enumerationvalue_id UUID NOT NULL
        CONSTRAINT fkf8d99ketatffxmapoax1upmo8
            REFERENCES datamodel.enumeration_value,
    classifier_id       UUID
        CONSTRAINT fkissxtxxag5rkhtjr2q1pivt64
            REFERENCES core.classifier,
    annotation_id       UUID
        CONSTRAINT fkso04vaqmba4n4ffdbx5gg0fly
            REFERENCES core.annotation,
    semantic_link_id    UUID
        CONSTRAINT fkrefs16rh5cjm8rwngb9ijw9y1
            REFERENCES core.semantic_link,
    reference_file_id   UUID
        CONSTRAINT fk40tuyaalgpyfdnp2wqfl1bl3b
            REFERENCES core.reference_file,
    metadata_id         UUID
        CONSTRAINT fk9xuiuctli6j5hra8j0pw0xbib
            REFERENCES core.metadata
);

ALTER TABLE datamodel.join_enumerationvalue_to_facet
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.summary_metadata (
    id                         UUID         NOT NULL
        CONSTRAINT summary_metadata_pkey
            PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    summary_metadata_type      VARCHAR(255) NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    catalogue_item_id          UUID,
    created_by                 VARCHAR(255) NOT NULL,
    label                      TEXT         NOT NULL,
    description                TEXT
);

ALTER TABLE datamodel.summary_metadata
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.join_dataclass_to_facet (
    dataclass_id        UUID NOT NULL
        CONSTRAINT fkc80l2pkf48a8sw4ijsudyaers
            REFERENCES datamodel.data_class,
    classifier_id       UUID
        CONSTRAINT fkgh9f6ok7n9wxwxopjku7yhfea
            REFERENCES core.classifier,
    annotation_id       UUID
        CONSTRAINT fkpqpxtrqg9jh2ick2ug9mhcfxt
            REFERENCES core.annotation,
    semantic_link_id    UUID
        CONSTRAINT fk7tq9mj4pasf5fmebs2sc9ap86
            REFERENCES core.semantic_link,
    reference_file_id   UUID
        CONSTRAINT fk5n6b907728hblnk0ihhwhbac4
            REFERENCES core.reference_file,
    metadata_id         UUID
        CONSTRAINT fkewipna2xjervio2w9rsem7vvu
            REFERENCES core.metadata,
    summary_metadata_id UUID
        CONSTRAINT fkgeoshkis2b6trtu8c5etvg72n
            REFERENCES datamodel.summary_metadata
);

ALTER TABLE datamodel.join_dataclass_to_facet
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.join_dataelement_to_facet (
    dataelement_id      UUID NOT NULL
        CONSTRAINT fkpsyiacoeuww886wy5apt5idwq
            REFERENCES datamodel.data_element,
    classifier_id       UUID
        CONSTRAINT fkdn8e1l2pofwmdpfroe9bkhskm
            REFERENCES core.classifier,
    annotation_id       UUID
        CONSTRAINT fke75uuv2w694ofrm1ogdqio495
            REFERENCES core.annotation,
    semantic_link_id    UUID
        CONSTRAINT fk8roq23ibhwodnpibdp1srk6aq
            REFERENCES core.semantic_link,
    reference_file_id   UUID
        CONSTRAINT fk89immwtwlrbwrel10gjy3yimw
            REFERENCES core.reference_file,
    metadata_id         UUID
        CONSTRAINT fkg58co9t99dfp0076vkn23hemy
            REFERENCES core.metadata,
    summary_metadata_id UUID
        CONSTRAINT fkqef1ustdtk1irqjnohxwhlsxf
            REFERENCES datamodel.summary_metadata
);

ALTER TABLE datamodel.join_dataelement_to_facet
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.join_datamodel_to_facet (
    datamodel_id        UUID NOT NULL
        CONSTRAINT fkb2bggjawxcb5pynsrnpwgw35q
            REFERENCES datamodel.data_model,
    classifier_id       UUID
        CONSTRAINT fk1ek18e3t2cki6fch7jmbbati0
            REFERENCES core.classifier,
    annotation_id       UUID
        CONSTRAINT fk1yt7axbg37bynceoy6p06a5pk
            REFERENCES core.annotation,
    semantic_link_id    UUID
        CONSTRAINT fkppqku5drbeh06ro6594sx7qpn
            REFERENCES core.semantic_link,
    version_link_id     UUID
        CONSTRAINT fkk8m8u0b9dd216qsjdkbbttqmu
            REFERENCES core.version_link,
    reference_file_id   UUID
        CONSTRAINT fkicjxoyym4mvpajl7amd2c96vg
            REFERENCES core.reference_file,
    metadata_id         UUID
        CONSTRAINT fkn8kvp5hpmtpu6t9ivldafifom
            REFERENCES core.metadata,
    summary_metadata_id UUID
        CONSTRAINT fkb1rfqfx6stfaote1vqbh0u65b
            REFERENCES datamodel.summary_metadata
);

ALTER TABLE datamodel.join_datamodel_to_facet
    OWNER TO maurodatamapper;

CREATE TABLE datamodel.join_datatype_to_facet (
    datatype_id         UUID NOT NULL
        CONSTRAINT fkka92tyn95wh23p9y7rjb1sila
            REFERENCES datamodel.data_type,
    classifier_id       UUID
        CONSTRAINT fkq73nqfoqdhodobkio53xnoroj
            REFERENCES core.classifier,
    annotation_id       UUID
        CONSTRAINT fks3obp3gh2qp7lvl7c2ke33672
            REFERENCES core.annotation,
    semantic_link_id    UUID
        CONSTRAINT fkgfuqffr58ihdup07r1ys2rsts
            REFERENCES core.semantic_link,
    reference_file_id   UUID
        CONSTRAINT fkk6htfwfpc5ty1o1skmlw0ct5h
            REFERENCES core.reference_file,
    metadata_id         UUID
        CONSTRAINT fk685o5rkte9js4kibmx3e201ul
            REFERENCES core.metadata,
    summary_metadata_id UUID
        CONSTRAINT fkxyctuwpfqyqog98xf69enu2y
            REFERENCES datamodel.summary_metadata
);

ALTER TABLE datamodel.join_datatype_to_facet
    OWNER TO maurodatamapper;

CREATE INDEX summarymetadata_created_by_idx
    ON datamodel.summary_metadata(created_by);

CREATE TABLE datamodel.summary_metadata_report (
    id                  UUID         NOT NULL
        CONSTRAINT summary_metadata_report_pkey
            PRIMARY KEY,
    version             BIGINT       NOT NULL,
    date_created        TIMESTAMP    NOT NULL,
    last_updated        TIMESTAMP    NOT NULL,
    report_date         TIMESTAMP    NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    report_value        TEXT         NOT NULL,
    summary_metadata_id UUID         NOT NULL
        CONSTRAINT fk9auhycixx3nly0xthx9eg8i8y
            REFERENCES datamodel.summary_metadata
);

ALTER TABLE datamodel.summary_metadata_report
    OWNER TO maurodatamapper;

CREATE INDEX summarymetadatareport_created_by_idx
    ON datamodel.summary_metadata_report(created_by);

CREATE INDEX summary_metadata_report_summary_metadata_idx
    ON datamodel.summary_metadata_report(summary_metadata_id);

