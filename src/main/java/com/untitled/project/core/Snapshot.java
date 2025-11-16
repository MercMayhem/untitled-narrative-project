package com.untitled.project.core;

import java.util.Optional;

import com.untitled.project.core.identifier.SnapshotIdentifier;
import com.untitled.project.core.identifier.generator.IdentifierGenerator;

public abstract class Snapshot<T> {
    private Optional<SnapshotIdentifier<T>> previous;
    private Optional<SnapshotIdentifier<T>> next;
    private DocumentCollection<T, SnapshotIdentifier<T>> collection;

    public Snapshot(IdentifierGenerator<SnapshotIdentifier<T>, T> snapshotIdGenerator, Optional<SnapshotIdentifier<T>> previous, DocumentCollection<T, SnapshotIdentifier<T>> collection) {
        this.previous = previous;
        this.collection = collection;
    }

    public final SnapshotIdentifier<T> getId() {
        return this.collection.getId();
    }

    public final Optional<SnapshotIdentifier<T>> getPrevious() {
        return this.previous;
    }

    public final Optional<SnapshotIdentifier<T>> getNext() {
        return this.next;
    }

    public final void setNext(Optional<SnapshotIdentifier<T>> next) {
        this.next = next;
    }
     
    public final void setPrevious(Optional<SnapshotIdentifier<T>> previous) {
        this.previous = previous;
    }
}