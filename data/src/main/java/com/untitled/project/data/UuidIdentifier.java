package com.untitled.project.data;

import java.util.UUID;

import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.SnapshotIdentifier;

public class UuidIdentifier implements DocumentIdentifier<UUID>, SnapshotIdentifier<UUID> {
    final UUID id;

    public UuidIdentifier(UUID id) {
        this.id = id;
    }

    @Override
    public UUID value() {
        return this.id;
    }

    @Override
    public String rawString() {
        return this.id.toString();
    }
}
