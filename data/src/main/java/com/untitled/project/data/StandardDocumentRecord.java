package com.untitled.project.data;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.port.StandardDocumentMapper;


public class StandardDocumentRecord implements StandardDocumentMapper<StandardDocumentRecord> {
    UUID id;
    String content;
    Optional<Instant> createdAt;
    Optional<Instant> updatedAt;

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
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
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = Optional.ofNullable(createdAt);
    }
    public Optional<Instant> getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Optional.ofNullable(updatedAt);
    }
    
    @Override
    public StandardDocumentRecord fromStandardDocument() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public StandardDocument toStandardDocument() {
        StandardDocument standardDocument = new StandardDocument(this.id, this.content);
        return standardDocument;
    }
}
