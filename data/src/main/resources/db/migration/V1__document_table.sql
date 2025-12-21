CREATE TABLE document (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 1
);

CREATE TABLE document_content(
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    document_id UUID NOT NULL,
    title VARCHAR(50) NOT NULL,
    content TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    version BIGINT NOT NULL DEFAULT 1,

    FOREIGN KEY(document_id) REFERENCES document(id) ON DELETE CASCADE
);