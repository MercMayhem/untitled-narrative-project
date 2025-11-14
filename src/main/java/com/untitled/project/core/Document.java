package com.untitled.project.core;

import java.util.Optional;

import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public abstract class Document<T> {
    private final DocumentIdentifier<T> id;
    private Optional<DocumentContent> content;

    public Document(IdentifierGenerator<DocumentIdentifier<T>, T> documentIdGenerator) {
        this.id = documentIdGenerator.generateUnique();
        this.content = Optional.empty();
    }

    public final DocumentIdentifier<T> getId() {
        return this.id;
    }

    public Optional<DocumentContent> getContent() {
        return this.content;
    }
}
