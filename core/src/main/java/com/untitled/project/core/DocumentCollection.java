package com.untitled.project.core;

import com.untitled.project.core.identifier.DocumentCollectionIdentifier;

public abstract class DocumentCollection<U, V extends DocumentCollectionIdentifier<U>, W extends DocumentCollectionInfo> {
    protected final V id;
    W info;

    public DocumentCollection(V id) {
        this.id = id;
    }

    public DocumentCollection(V id, W documentCollectionInfo) {
        this.id = id;
        this.info = documentCollectionInfo;
    }


    public V getId() {
        return id;
    }
    public W getInfo() {
        return info;
    }
}
