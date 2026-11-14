CREATE TABLE documents(
    id BIGINT PRIMARY KEY Generated always as IDENTITY,
    application_id BIGINT not NULL,
    document_type VARCHAR(40) not NULL,
    file_path VARCHAR(512) not NULL,
    format_file varchar(100),
    size_bytes BIGINT constraint valid_size check (size_bytes>0) not null,
    uploaded_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    constraint fk_documents_applications FOREIGN KEY (application_id)  REFERENCES applications(id)
);