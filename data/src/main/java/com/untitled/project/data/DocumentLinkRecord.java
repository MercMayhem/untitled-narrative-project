// DocumentLinkRecord.java
package com.untitled.project.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.UUID;
import java.util.Vector;

import com.untitled.project.models.document.UuidIdentifier;

public class DocumentLinkRecord {
    UUID sourceDocumentId;
    UUID targetDocumentId;
    Instant createdAt;

    DocumentLinkRecord() {
        this.createdAt = Instant.now();
    }

    DocumentLinkRecord(UUID sourceDocumentId, UUID targetDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
        this.targetDocumentId = targetDocumentId;
        this.createdAt = Instant.now();
    }

    public UUID getSourceDocumentId() {
        return sourceDocumentId;
    }

    public void setSourceDocumentId(UUID sourceDocumentId) {
        this.sourceDocumentId = sourceDocumentId;
    }

    public UUID getTargetDocumentId() {
        return targetDocumentId;
    }

    public void setTargetDocumentId(UUID targetDocumentId) {
        this.targetDocumentId = targetDocumentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Static method to insert/link documents with version checking
    public static int link(UuidIdentifier sourceDocument,
                           UuidIdentifier targetDocument,
                           Connection connection) throws SQLException {
        String insertLinkSql =
                "INSERT INTO document_link (source_document_id, target_document_id) " +
                        "SELECT d1.id, d2.id " +
                        "FROM document d1, document d2 " +
                        "WHERE d1.id = ? AND d1.version = ? " +
                        "AND d2.id = ? AND d2.version = ? " +
                        "ON CONFLICT (source_document_id, target_document_id) DO UPDATE SET " +
                        "created_at = NOW()";

        try (PreparedStatement stmt = connection.prepareStatement(insertLinkSql)) {
            stmt.setObject(1, sourceDocument.value(), Types.OTHER);
            stmt.setLong(2, sourceDocument.getVersion());
            stmt.setObject(3, targetDocument.value(), Types.OTHER);
            stmt.setLong(4, targetDocument.getVersion());

            return stmt.executeUpdate();
        }
    }

    // Static method to delete/unlink documents with version checking
    public static int unlink(UuidIdentifier sourceDocument,
                             UuidIdentifier targetDocument,
                             Connection connection) throws SQLException {
        String deleteLinkSql =
                "DELETE FROM document_link " +
                        "WHERE source_document_id = ? " +
                        "AND target_document_id = ? " +
                        "AND EXISTS (SELECT 1 FROM document WHERE id = ? AND version = ?) " +
                        "AND EXISTS (SELECT 1 FROM document WHERE id = ? AND version = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(deleteLinkSql)) {
            stmt.setObject(1, sourceDocument.value(), Types.OTHER);
            stmt.setObject(2, targetDocument.value(), Types.OTHER);
            stmt.setObject(3, sourceDocument.value(), Types.OTHER);
            stmt.setLong(4, sourceDocument.getVersion());
            stmt.setObject(5, targetDocument.value(), Types.OTHER);
            stmt.setLong(6, targetDocument.getVersion());

            return stmt.executeUpdate();
        }
    }

    // Static method to get outgoing links for a document
    public static Vector<DocumentLinkRecord> getOutgoingLinks(UUID documentId, Connection connection) throws SQLException {
        Vector<DocumentLinkRecord> records = new Vector<>();

        String getOutgoingLinksSql =
                "SELECT source_document_id, target_document_id, created_at " +
                        "FROM document_link " +
                        "WHERE source_document_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(getOutgoingLinksSql)) {
            stmt.setObject(1, documentId, Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID sourceId = rs.getObject("source_document_id", UUID.class);
                    UUID targetId = rs.getObject("target_document_id", UUID.class);
                    Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);

                    Instant createdAt = null;
                    if (createdAtTimestamp != null) {
                        createdAt = createdAtTimestamp.toInstant();
                    }

                    DocumentLinkRecord record = new DocumentLinkRecord();
                    record.setSourceDocumentId(sourceId);
                    record.setTargetDocumentId(targetId);
                    record.setCreatedAt(createdAt);

                    records.add(record);
                }
            }
        }

        return records;
    }

    // Static method to get incoming links for a document
    public static Vector<DocumentLinkRecord> getIncomingLinks(UUID documentId, Connection connection) throws SQLException {
        Vector<DocumentLinkRecord> records = new Vector<>();

        String getIncomingLinksSql =
                "SELECT source_document_id, target_document_id, created_at " +
                        "FROM document_link " +
                        "WHERE target_document_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(getIncomingLinksSql)) {
            stmt.setObject(1, documentId, Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID sourceId = rs.getObject("source_document_id", UUID.class);
                    UUID targetId = rs.getObject("target_document_id", UUID.class);
                    Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);

                    Instant createdAt = null;
                    if (createdAtTimestamp != null) {
                        createdAt = createdAtTimestamp.toInstant();
                    }

                    DocumentLinkRecord record = new DocumentLinkRecord();
                    record.setSourceDocumentId(sourceId);
                    record.setTargetDocumentId(targetId);
                    record.setCreatedAt(createdAt);

                    records.add(record);
                }
            }
        }

        return records;
    }
}