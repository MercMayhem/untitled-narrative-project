package com.untitled.project.core;

import java.util.List;

import com.untitled.project.core.identifier.SnapshotIdentifier;

public interface Snapshot<T, U> {
    public SnapshotIdentifier<T> getId();
    public List<Document<U>> getDocuments();
    public void addDocument(Document<U> document);
}
