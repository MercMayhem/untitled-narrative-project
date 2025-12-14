CREATE TABLE document (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    content TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);