package com.untitled.project.core;

import java.util.Optional;

import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public abstract class Document<T, U extends DocumentIdentifier<T>, V extends DocumentContent> {
    private final U id;
    private Optional<V> content;

    public Document(IdentifierGenerator<U, T> documentIdGenerator) {
        this.id = documentIdGenerator.generateUnique();
        this.content = Optional.empty();
    }

    public final U getId() {
        return this.id;
    }

    public Optional<V> getContent() {
        return this.content;
    }

    public void setContent(Optional<V> content) {
        this.content = content;
    }
}
