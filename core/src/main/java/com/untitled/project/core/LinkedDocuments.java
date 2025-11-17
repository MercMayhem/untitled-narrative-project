package com.untitled.project.core;

import java.util.HashSet;
import java.util.Optional;

public class LinkedDocuments<T> {
    private final HashSet<Document<T>> documents;
    private final Optional<Integer> remaining;

    public LinkedDocuments(HashSet<Document<T>> documents, Optional<Integer> remaining){
        this.documents = documents;
        this.remaining = remaining;
    }

    public Optional<Integer> getRemaining() {
        return remaining;
    }

    public HashSet<Document<T>> getDocuments() {
        return documents;
    }
}