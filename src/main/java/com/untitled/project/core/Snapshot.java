package com.untitled.project.core;

import java.util.Optional;

import com.untitled.project.core.identifier.SnapshotIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public abstract class Snapshot<T, DocID> {
    private Optional<SnapshotIdentifier<T>> previous;
    private DocumentCollection<DocID, T, SnapshotIdentifier<T>> collection;

    public Snapshot(IdentifierGenerator<SnapshotIdentifier<T>, T> snapshotIdGenerator, Optional<SnapshotIdentifier<T>> previous) {
        this.previous = previous;
    }    

    public final SnapshotIdentifier<T> getId() {
        return this.collection.getId();
    }

    public final Optional<SnapshotIdentifier<T>> getPrevious() {
        return this.previous;
    }
}