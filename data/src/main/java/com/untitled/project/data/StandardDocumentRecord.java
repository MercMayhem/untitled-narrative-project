package com.untitled.project.data;

import java.lang.classfile.ClassFile.Option;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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

    public static Optional<StandardDocumentRecord> get(UUID id, Connection connection) throws SQLException {
        StandardDocumentRecord record = null;

        String getDocumentRecordSql = 
            "SELECT id, created_at, updated_at, version FROM document WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(getDocumentRecordSql)) {
            pstmt.setObject(1, id, Types.OTHER);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Instant createdAt = rs.getObject("created_at", Timestamp.class).toInstant();
                Instant updatedAt = rs.getObject("updated_at", Timestamp.class).toInstant();
                Long version = rs.getLong("version");

                record = new StandardDocumentRecord();
                record.setId(id);
                record.setCreatedAt(createdAt);
                record.setUpdatedAt(updatedAt);
                record.setVersion(version);
            }
        }

        return Optional.ofNullable(record);
    }
}
