-- DDL for PostgreSQL (SKLTP Engagemangsindex / EI)
CREATE TABLE engagement_index_table (
    id VARCHAR(64) NOT NULL,
    business_object_instance_id VARCHAR(128) NOT NULL,
    categorization VARCHAR(255) NOT NULL,
    clinical_process_interest_id VARCHAR(128) NOT NULL,
    creation_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    data_controller VARCHAR(64) NOT NULL,
    logical_address VARCHAR(64) NOT NULL,
    most_recent_content TIMESTAMP WITHOUT TIME ZONE,
    owner VARCHAR(64) NOT NULL,
    registered_resident_id VARCHAR(32) NOT NULL,
    service_domain VARCHAR(255) NOT NULL,
    source_system VARCHAR(64) NOT NULL,
    update_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_engagement_index_table PRIMARY KEY (id)
);

CREATE INDEX engagement_search_index ON engagement_index_table (
    registered_resident_id,
    service_domain,
    categorization
);
