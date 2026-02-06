package com.untitled.project.data;

import java.sql.*;
import java.util.UUID;
import java.util.Vector;

import com.untitled.project.models.document.UuidIdentifier;

public class DocumentCollectionAssociationRecord {
    private UUID documentId;
    private UUID collectionId;

    public DocumentCollectionAssociationRecord() {}

    public DocumentCollectionAssociationRecord(UUID documentId, UUID collectionId) {
        this.documentId = documentId;
        this.collectionId = collectionId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(UUID collectionId) {
        this.collectionId = collectionId;
    }

    // Add document to collection with version checking
    public static int add(UuidIdentifier documentId, UuidIdentifier collectionId, Connection connection) throws SQLException {
        String insertSql =
                "INSERT INTO document_document_collection_xref (document_id, collection_id) " +
                        "SELECT d.id, ? " +
                        "FROM document d " +
                        "WHERE d.id = ? AND d.version = ? " +
                        "ON CONFLICT (document_id, collection_id) DO NOTHING";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, collectionId.value(), Types.OTHER);
            stmt.setObject(2, documentId.value(), Types.OTHER);
            stmt.setLong(3, documentId.getVersion());

            return stmt.executeUpdate();
        }
    }

    // Remove document from collection with version checking
    public static int remove(UuidIdentifier documentId, UuidIdentifier collectionId, Connection connection) throws SQLException {
        String deleteSql =
                "DELETE FROM document_document_collection_xref " +
                        "WHERE document_id = ? " +
                        "AND collection_id = ? " +
                        "AND EXISTS (SELECT 1 FROM document WHERE id = ? AND version = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setObject(1, documentId.value(), Types.OTHER);
            stmt.setObject(2, collectionId.value(), Types.OTHER);
            stmt.setObject(3, documentId.value(), Types.OTHER);
            stmt.setLong(4, documentId.getVersion());

            return stmt.executeUpdate();
        }
    }

    // Get all documents in a collection with pagination
    public static Vector<DocumentCollectionAssociationRecord> getDocumentsInCollection(
            UuidIdentifier collectionId, int pageNumber, int pageSize, Connection connection) throws SQLException {
        Vector<DocumentCollectionAssociationRecord> records = new Vector<>();

        int offset = (pageNumber - 1) * pageSize;

        String selectSql =
                "SELECT ddcx.document_id, ddcx.collection_id " +
                        "FROM document_document_collection_xref ddcx " +
                        "JOIN document d ON ddcx.document_id = d.id " +
                        "WHERE ddcx.collection_id = ? " +
                        "ORDER BY d.created_at DESC " +
                        "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setObject(1, collectionId.value(), Types.OTHER);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentCollectionAssociationRecord record = new DocumentCollectionAssociationRecord();
                    record.setDocumentId(rs.getObject("document_id", UUID.class));
                    record.setCollectionId(rs.getObject("collection_id", UUID.class));
                    records.add(record);
                }
            }
        }

        return records;
    }

    public static Vector<DocumentCollectionAssociationRecord> getDocumentsInCollection(UuidIdentifier collectionId, Connection connection) throws SQLException {
        Vector<DocumentCollectionAssociationRecord> records = new Vector<>();

        String selectSql =
                "SELECT document_id, collection_id " +
                        "FROM document_document_collection_xref " +
                        "WHERE collection_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setObject(1, collectionId.value(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentCollectionAssociationRecord record = new DocumentCollectionAssociationRecord();
                    record.setDocumentId(rs.getObject("document_id", UUID.class));
                    record.setCollectionId(rs.getObject("collection_id", UUID.class));
                    records.add(record);
                }
            }
        }

        return records;
    }

    // Get total count of documents in a collection
    public static long getDocumentCountInCollection(UuidIdentifier collectionId, Connection connection) throws SQLException {
        String countSql =
                "SELECT COUNT(*) as total " +
                        "FROM document_document_collection_xref " +
                        "WHERE collection_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(countSql)) {
            stmt.setObject(1, collectionId.value(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        }

        return 0;
    }

    // Get all collections for a document with pagination
    public static Vector<DocumentCollectionAssociationRecord> getCollectionsForDocument(
            UuidIdentifier documentId, int pageNumber, int pageSize, Connection connection) throws SQLException {
        Vector<DocumentCollectionAssociationRecord> records = new Vector<>();

        int offset = (pageNumber - 1) * pageSize;

        String selectSql =
                "SELECT ddcx.document_id, ddcx.collection_id " +
                        "FROM document_document_collection_xref ddcx " +
                        "JOIN document_collection dc ON ddcx.collection_id = dc.id " +
                        "WHERE ddcx.document_id = ? " +
                        "ORDER BY dc.created_at DESC " +
                        "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setObject(1, documentId.value(), Types.OTHER);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentCollectionAssociationRecord record = new DocumentCollectionAssociationRecord();
                    record.setDocumentId(rs.getObject("document_id", UUID.class));
                    record.setCollectionId(rs.getObject("collection_id", UUID.class));
                    records.add(record);
                }
            }
        }

        return records;
    }

    public static Vector<DocumentCollectionAssociationRecord> getCollectionsForDocument(UuidIdentifier documentId, Connection connection) throws SQLException {
        Vector<DocumentCollectionAssociationRecord> records = new Vector<>();

        String selectSql =
                "SELECT document_id, collection_id " +
                        "FROM document_document_collection_xref " +
                        "WHERE document_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setObject(1, documentId.value(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentCollectionAssociationRecord record = new DocumentCollectionAssociationRecord();
                    record.setDocumentId(rs.getObject("document_id", UUID.class));
                    record.setCollectionId(rs.getObject("collection_id", UUID.class));
                    records.add(record);
                }
            }
        }

        return records;
    }

    // Get total count of collections for a document
    public static long getCollectionCountForDocument(UuidIdentifier documentId, Connection connection) throws SQLException {
        String countSql =
                "SELECT COUNT(*) as total " +
                        "FROM document_document_collection_xref " +
                        "WHERE document_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(countSql)) {
            stmt.setObject(1, documentId.value(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
        }

        return 0;
    }
}