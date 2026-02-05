CREATE TABLE document_link (
    source_document_id UUID NOT NULL,
    target_document_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (source_document_id, target_document_id),
    FOREIGN KEY (source_document_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY (target_document_id) REFERENCES document(id) ON DELETE CASCADE,

    CHECK (source_document_id != target_document_id)
);

CREATE INDEX idx_document_link_target ON document_link(target_document_id);