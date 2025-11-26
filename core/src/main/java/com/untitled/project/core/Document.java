package com.untitled.project.core;

import java.util.Optional;

import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public abstract class Document<T, U extends DocumentIdentifier<T>> {
    private final U id;
    private Optional<DocumentContent> content;
    private Optional<LinkedDocuments<T, U>> linkedDocuments;

    public Document(IdentifierGenerator<U, T> documentIdGenerator) {
        this.id = documentIdGenerator.generateUnique();
        this.content = Optional.empty();
    }

    public final DocumentIdentifier<T> getId() {
        return this.id;
    }

    public Optional<DocumentContent> getContent() {
        return this.content;
    }

    public void setContent(Optional<DocumentContent> content) {
        this.content = content;
    }

    public Optional<LinkedDocuments<T, U>> getLinkedDocuments() {
        return linkedDocuments;
    }

    public void setLinkedDocuments(Optional<LinkedDocuments<T, U>> linkedDocuments) {
        this.linkedDocuments = linkedDocuments;
    }
}
