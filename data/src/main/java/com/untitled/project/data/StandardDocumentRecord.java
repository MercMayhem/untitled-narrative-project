package com.untitled.project.data;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


public class StandardDocumentRecord {
    UUID id;
    Optional<Instant> createdAt;
    Optional<Instant> updatedAt;
    Long version;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public Optional<Instant> getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = Optional.ofNullable(createdAt);
    }
    public Optional<Instant> getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Optional.ofNullable(updatedAt);
    }

    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}
