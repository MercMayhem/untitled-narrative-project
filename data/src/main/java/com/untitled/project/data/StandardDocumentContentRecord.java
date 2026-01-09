package com.untitled.project.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;

import com.untitled.project.models.document.StandardDocumentContentEntry;
import com.untitled.project.models.document.UuidIdentifier;

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

    StandardDocumentContentRecord(UuidIdentifier contentIdentifier, StandardDocumentContentEntry entry, UuidIdentifier documentIdentifier){
        this.id = contentIdentifier.value();
        this.documentId = documentIdentifier.value();
        this.title = entry.getTitle();
        this.content = entry.getContent();
        this.version = contentIdentifier.getVersion();
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

    public static Vector<StandardDocumentContentRecord> get(UUID documentId, Connection connection) throws SQLException {
        Vector<StandardDocumentContentRecord> records = new Vector<>();

        String getDocumentContentRecordSql = 
            "SELECT id, title, content, created_at, updated_at, version FROM document_content WHERE document_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(getDocumentContentRecordSql)) {
            pstmt.setObject(1, documentId, Types.OTHER);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UUID id = rs.getObject("id", UUID.class);
                String title = rs.getString("title");
                String content = rs.getString("content");
                
                Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);
                Timestamp updatedAtTimestamp = rs.getObject("updated_at", Timestamp.class);
                
                Instant createdAt = null;
                if (createdAtTimestamp != null) {
                    createdAt = createdAtTimestamp.toInstant();
                }

                Instant updatedAt = null;
                if (updatedAtTimestamp != null) {
                    updatedAt = updatedAtTimestamp.toInstant();
                }

                Long version = rs.getLong("version");

                StandardDocumentContentRecord record = new StandardDocumentContentRecord();
                record.setId(id);
                record.setTitle(title);
                record.setContent(content);
                record.setCreatedAt(createdAt);
                record.setUpdatedAt(updatedAt);
                record.setVersion(version);

                records.add(record);
            }
        }

        return records;
    }
}
