package com.untitled.project.core;

import java.util.HashSet;
import java.util.Optional;

import com.untitled.project.core.identifier.DocumentIdentifier;

public class LinkedDocuments<T, U extends DocumentIdentifier<T>> {
    private final HashSet<Document<T, U>> documents;
    private final Optional<Integer> remaining;

    public LinkedDocuments(HashSet<Document<T, U>> documents, Optional<Integer> remaining){
        this.documents = documents;
        this.remaining = remaining;
    }

    public Optional<Integer> getRemaining() {
        return remaining;
    }

    public HashSet<Document<T, U>> getDocuments() {
        return documents;
    }
}