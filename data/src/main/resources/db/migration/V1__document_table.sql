CREATE TABLE document (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    content TEXT,
    internal_id TEXT UNIQUE NOT NULL,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);