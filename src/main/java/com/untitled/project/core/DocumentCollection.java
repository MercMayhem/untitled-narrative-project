package com.untitled.project.core;

import com.untitled.project.core.identifier.DocumentCollectionIdentifier;

public abstract class DocumentCollection<T, U, V extends DocumentCollectionIdentifier<U>> {
    protected final V id;

    public DocumentCollection(V id) {
        this.id = id;
    }

    public V getId() {
        return id;
    }
}
