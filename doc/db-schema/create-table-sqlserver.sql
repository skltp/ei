-- DDL for Microsoft SQL Server (SKLTP Engagemangsindex / EI)
CREATE TABLE engagement_index_table (
    id NVARCHAR(64) NOT NULL,
    business_object_instance_id NVARCHAR(128) NOT NULL,
    categorization NVARCHAR(255) NOT NULL,
    clinical_process_interest_id NVARCHAR(128) NOT NULL,
    creation_time DATETIME2 NOT NULL,
    data_controller NVARCHAR(64) NOT NULL,
    logical_address NVARCHAR(64) NOT NULL,
    most_recent_content DATETIME2 NULL,
    owner NVARCHAR(64) NOT NULL,
    registered_resident_id NVARCHAR(32) NOT NULL,
    service_domain NVARCHAR(255) NOT NULL,
    source_system NVARCHAR(64) NOT NULL,
    update_time DATETIME2 NULL,
    CONSTRAINT PK_engagement_index_table PRIMARY KEY (id)
);

CREATE INDEX engagement_search_index ON engagement_index_table (
    registered_resident_id,
    service_domain,
    categorization
);
