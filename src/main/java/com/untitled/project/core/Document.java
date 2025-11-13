package com.untitled.project.core;

import java.util.HashSet;
import java.util.UUID;

import com.untitled.project.core.identifier.DocumentIdentifier;

public class Document<T> {
    private final UUID id;
    HashSet<DocumentIdentifier<T>> connectedDocumentsUuids;

    public Document() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return this.id;
    }
}
