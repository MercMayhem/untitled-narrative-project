package com.untitled.project.core;

import java.util.HashSet;

import com.untitled.project.core.identifier.DocumentCollectionIdentifier;
import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public class Document<T, U> {
    private final DocumentIdentifier<T> id;
    HashSet<DocumentCollectionIdentifier<U>> documentCollectionId;
    HashSet<DocumentIdentifier<T>> connectedDocumentsIdentifiers;

    public Document(HashSet<DocumentCollectionIdentifier<U>> documentCollectionId, IdentifierGenerator<DocumentIdentifier<T>, T> documentIdGenerator) {
        this.documentCollectionId = documentCollectionId;
        this.id = documentIdGenerator.generateUnique();
    }

    public DocumentIdentifier<T> getId() {
        return this.id;
    }
}
