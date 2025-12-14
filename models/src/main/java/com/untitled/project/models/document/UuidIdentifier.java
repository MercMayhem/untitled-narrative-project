package com.untitled.project.models.document;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UuidIdentifier that = (UuidIdentifier) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
