CREATE TABLE document_collection (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE document_document_collection_xref(
    document_id UUID NOT NULL,
    collection_id UUID NOT NULL,

    PRIMARY KEY(document_id, collection_id),
    FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY(collection_id) REFERENCES document_collection(id) ON DELETE CASCADE
);