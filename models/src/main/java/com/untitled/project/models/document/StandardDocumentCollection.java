package com.untitled.project.models.document;

import com.untitled.project.core.DocumentCollection;
import com.untitled.project.models.util.Page;

import java.util.UUID;

public class StandardDocumentCollection extends DocumentCollection<UUID, UuidIdentifier, StandardDocumentCollectionInfo> {
    Page<StandardDocument> documents;
    Page<UuidIdentifier> documentIdentifierPage;

    public Page<UuidIdentifier> getDocumentIdentifierPage() {
        return documentIdentifierPage;
    }

    public void setDocumentIdentifierPage(Page<UuidIdentifier> documentIdentifierPage) {
        this.documentIdentifierPage = documentIdentifierPage;
    }

    public StandardDocumentCollection(UuidIdentifier id) {
        super(id);
    }

    public StandardDocumentCollection(StandardDocumentCollectionInfo documentCollectionInfo) {
        UuidIdentifier id = new UuidIdentifierGenerator().generateUnique();
        super(id, documentCollectionInfo);
    }


    public StandardDocumentCollection(UuidIdentifier id, StandardDocumentCollectionInfo documentCollectionInfo) {
        super(id, documentCollectionInfo);
    }

    public Page<StandardDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(Page<StandardDocument> documents) {
        this.documents = documents;
    }
}
