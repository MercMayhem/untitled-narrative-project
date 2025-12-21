package com.untitled.project.data;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class StandardDocumentContentRecord {
    UUID id;
    UUID documentId;
    String title;
    String content;
    Optional<Instant> createdAt;
    Optional<Instant> updatedAt;
    Long version;

    StandardDocumentContentRecord(){
        this.createdAt = Optional.of(Instant.now());
        this.updatedAt = Optional.of(Instant.now());
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getDocumentId() {
        return documentId;
    }
    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Optional<Instant> getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Optional<Instant> createdAt) {
        this.createdAt = createdAt;
    }
    public Optional<Instant> getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Optional<Instant> updatedAt) {
        this.updatedAt = updatedAt;
    }
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}
