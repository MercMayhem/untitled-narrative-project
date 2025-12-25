package com.untitled.project.models.document;

import java.util.UUID;

import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.identifier.SnapshotIdentifier;

public class UuidIdentifier implements DocumentIdentifier<UUID>, SnapshotIdentifier<UUID> {
    final UUID id;
    Long version;

    public UuidIdentifier(UUID id, Long version) {
        this.id = id;
        this.version = version;
    }

    public UuidIdentifier(UUID id) {
        this.id = id;
        this.version = Long.valueOf(1);
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "UuidIdentifier [id=" + id + ", version=" + version + ", hashCode()=" + hashCode() + "]";
    }
}
