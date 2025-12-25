package com.untitled.project.models.document;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.untitled.project.core.Document;

public class StandardDocument extends Document<UUID, UuidIdentifier, StandardDocumentContent> {

    public StandardDocument(UUID id, HashMap<UuidIdentifier, StandardDocumentContentEntry> content, Long version) {
        UuidIdentifier identifier = new UuidIdentifier(id, version);
        StandardDocumentContent documentContent = new StandardDocumentContent(content);
        super(identifier, documentContent);
    }

    public StandardDocument(HashMap<UuidIdentifier, StandardDocumentContentEntry> content) {
        UuidIdentifierGenerator uuidIdentifierGenerator = new UuidIdentifierGenerator();
        StandardDocumentContent documentContent = new StandardDocumentContent(content);
        super(uuidIdentifierGenerator, documentContent);
    }

    public StandardDocument(){
        UuidIdentifierGenerator uuidIdentifierGenerator = new UuidIdentifierGenerator();
        super(uuidIdentifierGenerator);
    }

    @Override
    public Optional<StandardDocumentContent> getContent() {
        // TODO Auto-generated method stub
        return super.getContent();
    }

    @Override
    public void setContent(Optional<StandardDocumentContent> content) {
        // TODO Auto-generated method stub
        super.setContent(content);
    }

}
