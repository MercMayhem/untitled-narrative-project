package com.untitled.project.core;

import com.untitled.project.core.identifier.DocumentCollectionIdentifier;
import com.untitled.project.core.identifier.DocumentIdentifier;

public abstract class DocumentCollection<T, U, V extends DocumentCollectionIdentifier<U>> {
    protected final V id;
    public abstract boolean addDocument(Document<T> document);
    public abstract boolean deleteDocument(DocumentIdentifier<T> documentIdentifier);

    public DocumentCollection(V id) {
        this.id = id;
    }

    public V getId() {
        return id;
    }
}
