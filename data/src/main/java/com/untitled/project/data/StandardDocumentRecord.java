package com.untitled.project.data;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.port.StandardDocumentMapper;


public class StandardDocumentRecord implements StandardDocumentMapper<StandardDocumentRecord> {
    UUID id;
    String content;
    String internalId;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;

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
    public String getInternalId() {
        return internalId;
    }
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }
    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public StandardDocumentRecord fromStandardDocument() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public StandardDocument toStandardDocument() {
        // TODO Auto-generated method stub
        return null;
    }
}
