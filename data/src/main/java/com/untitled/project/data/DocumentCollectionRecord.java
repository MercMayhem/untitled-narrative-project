package com.untitled.project.data;

import com.untitled.project.models.document.UuidIdentifier;

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

public class DocumentCollectionRecord {
    private UUID id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public DocumentCollectionRecord() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    public DocumentCollectionRecord(UUID id, String title) {
        this.id = id;
        this.title = title;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Insert a new collection
    public static int insert(DocumentCollectionRecord record, Connection connection) throws SQLException {
        String insertSql =
                "INSERT INTO document_collection (id, title, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setObject(1, record.getId(), Types.OTHER);
            stmt.setString(2, record.getTitle());
            stmt.setTimestamp(3, Timestamp.from(record.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.from(record.getUpdatedAt()));

            return stmt.executeUpdate();
        }
    }

    // Get collection by ID
    public static Optional<DocumentCollectionRecord> get(UuidIdentifier id, Connection connection) throws SQLException {
        String selectSql =
                "SELECT id, title, created_at, updated_at FROM document_collection WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setObject(1, id.value(), Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DocumentCollectionRecord record = new DocumentCollectionRecord();
                    record.setId(rs.getObject("id", UUID.class));
                    record.setTitle(rs.getString("title"));

                    Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);
                    if (createdAtTimestamp != null) {
                        record.setCreatedAt(createdAtTimestamp.toInstant());
                    }

                    Timestamp updatedAtTimestamp = rs.getObject("updated_at", Timestamp.class);
                    if (updatedAtTimestamp != null) {
                        record.setUpdatedAt(updatedAtTimestamp.toInstant());
                    }

                    return Optional.of(record);
                }
            }
        }

        return Optional.empty();
    }

    // Get all collections
    public static Vector<DocumentCollectionRecord> getAll(Connection connection) throws SQLException {
        Vector<DocumentCollectionRecord> records = new Vector<>();

        String selectAllSql =
                "SELECT id, title, created_at, updated_at FROM document_collection ORDER BY created_at DESC";

        try (PreparedStatement stmt = connection.prepareStatement(selectAllSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DocumentCollectionRecord record = new DocumentCollectionRecord();
                record.setId(rs.getObject("id", UUID.class));
                record.setTitle(rs.getString("title"));

                Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);
                if (createdAtTimestamp != null) {
                    record.setCreatedAt(createdAtTimestamp.toInstant());
                }

                Timestamp updatedAtTimestamp = rs.getObject("updated_at", Timestamp.class);
                if (updatedAtTimestamp != null) {
                    record.setUpdatedAt(updatedAtTimestamp.toInstant());
                }

                records.add(record);
            }
        }

        return records;
    }

    // Update collection
    public static int update(DocumentCollectionRecord record, Connection connection) throws SQLException {
        String updateSql =
                "UPDATE document_collection " +
                        "SET title = ?, updated_at = ? " +
                        "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, record.getTitle());
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.setObject(3, record.getId(), Types.OTHER);

            return stmt.executeUpdate();
        }
    }

    // Delete collection
    public static int delete(UuidIdentifier id, Connection connection) throws SQLException {
        String deleteSql = "DELETE FROM document_collection WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setObject(1, id.value(), Types.OTHER);

            return stmt.executeUpdate();
        }
    }
    public static Vector<DocumentCollectionRecord> getPaginated(int pageNumber, int pageSize, Connection connection) throws SQLException {
        Vector<DocumentCollectionRecord> records = new Vector<>();

        int offset = (pageNumber - 1) * pageSize;

        String selectAllSql =
                "SELECT id, title, created_at, updated_at " +
                        "FROM document_collection " +
                        "ORDER BY created_at DESC " +
                        "LIMIT ? OFFSET ?";

        try (PreparedStatement stmt = connection.prepareStatement(selectAllSql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentCollectionRecord record = new DocumentCollectionRecord();
                    record.setId(rs.getObject("id", UUID.class));
                    record.setTitle(rs.getString("title"));

                    Timestamp createdAtTimestamp = rs.getObject("created_at", Timestamp.class);
                    if (createdAtTimestamp != null) {
                        record.setCreatedAt(createdAtTimestamp.toInstant());
                    }

                    Timestamp updatedAtTimestamp = rs.getObject("updated_at", Timestamp.class);
                    if (updatedAtTimestamp != null) {
                        record.setUpdatedAt(updatedAtTimestamp.toInstant());
                    }

                    records.add(record);
                }
            }
        }

        return records;
    }

    public static long getTotalCount(Connection connection) throws SQLException {
        String countSql = "SELECT COUNT(*) as total FROM document_collection";

        try (PreparedStatement stmt = connection.prepareStatement(countSql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("total");
            }
        }

        return 0;
    }
}