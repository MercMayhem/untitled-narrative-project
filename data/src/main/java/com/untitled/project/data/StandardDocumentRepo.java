package com.untitled.project.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import com.untitled.project.core.Document;
import com.untitled.project.models.document.StandardDocumentContentEntry;
import com.untitled.project.models.document.StandardDocumentContent;
import com.untitled.project.models.document.UuidIdentifier;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class StandardDocumentRepo {
    private static volatile HikariDataSource db;
    
    public StandardDocumentRepo(HikariConfig cfg) {
        if (db == null) {
            synchronized (StandardDocumentRepo.class) {
                if (db == null) {
                    db = new HikariDataSource(cfg);
                }
            }
        }
    }

    public StandardDocumentRepo() {
        if (db == null) {
            throw new IllegalStateException("Database not initialized");
        }
    }

    static HikariDataSource ds() {
        return db;
    }

    public void deleteDocument(UuidIdentifier id, Connection connection) {
        // TODO Auto-generated method stub
        
    }

    public StandardDocumentRecord getDocumentById(UuidIdentifier id) throws SQLException{
        // TODO: change implementation
        return null;
    }

    public void linkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
        // TODO Auto-generated method stub
        
    }

    private boolean insertDocumentIfNew(Document<UUID, UuidIdentifier, StandardDocumentContent> document, Connection connection) throws SQLException {
        String insertDocumentSql = 
            "INSERT INTO document(id) VALUES (?)"
            + " ON CONFLICT DO NOTHING";
        
        int inserted;
        try (PreparedStatement stmt = connection.prepareStatement(insertDocumentSql)) {
            stmt.setObject(1, document.getId().value(), Types.OTHER);
            inserted = stmt.executeUpdate();
        }

        if (inserted == 0) {
            String insertDocumentContentSql = 
                "INSERT INTO document_content(id, document_id, title, content)"
                + " VALUES (?, ?, ?, ?)";

            
            if (document.getContent().isPresent()) {
                StandardDocumentContent content = document.getContent().get();

                try (PreparedStatement stmt = connection.prepareStatement(insertDocumentContentSql)){
                    for (Map.Entry<UuidIdentifier, StandardDocumentContentEntry> entry : content.getContent().entrySet()) {
                        UuidIdentifier id = entry.getKey();
                        StandardDocumentContentEntry contentData = entry.getValue();
                        stmt.setObject(1, id.value(), Types.OTHER);
                        stmt.setObject(2, document.getId().value(), Types.OTHER);
                        stmt.setString(3, contentData.getTitle());
                        stmt.setString(4, contentData.getContent());
                        stmt.addBatch();
                    }
                    
                    stmt.executeBatch();
                }
            }

            return true;
        }

        return false;
    }

    private class UpdateOrReturnExistingReturnValues {
        public HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent;
        public Vector<StandardDocumentContentRecord> createDocumentContent;
        
        public UpdateOrReturnExistingReturnValues(
            HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent,
            Vector<StandardDocumentContentRecord> createDocumentContent
        ) {
            this.conflictDocumentContent = conflictDocumentContent;
            this.createDocumentContent = createDocumentContent;
        } 
    }

    private UpdateOrReturnExistingReturnValues updateOrReturnExisting(Document<UUID, UuidIdentifier, StandardDocumentContent> document, Connection connection) throws SQLException {
        HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent = new HashMap<>();
        Vector<StandardDocumentContentRecord> createDocumentContent = new Vector<>();
        StandardDocumentContent content = document.getContent().get();

        String updateDocumentContentSql = 
            " UPDATE document_content"
            + " SET"
            + " version = version + 1,"
            + " title = ?,"
            + " content = ?,"
            + " updated_at =  now()"
            + " WHERE id = ? AND version = ?"
            + " RETURNING *, true as updated";

        String updateOrReturnExistingVersionSql = 
            "WITH updated AS (" + updateDocumentContentSql + ")"
            + " SELECT *"
            + " FROM updated"
            + " UNION ALL"
            + " SELECT *, false as updated"
            + " FROM document_content"
            + " WHERE id = ?"
            + " AND NOT EXISTS(SELECT 1 FROM updated)";

        try (PreparedStatement stmt = connection.prepareStatement(updateOrReturnExistingVersionSql)){
            for (Map.Entry<UuidIdentifier, StandardDocumentContentEntry> entry : content.getContent().entrySet()) {
                UuidIdentifier id = entry.getKey();
                StandardDocumentContentEntry contentData = entry.getValue();
                stmt.setString(1, contentData.getTitle());
                stmt.setString(2, contentData.getContent());
                stmt.setObject(3, id.value(), Types.OTHER);
                stmt.setLong(4, id.getVersion());
                stmt.setObject(5, id.value(), Types.OTHER);

                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    StandardDocumentContentRecord record = new StandardDocumentContentRecord();

                    record.setId(id.value());
                    record.setDocumentId(document.getId().value());
                    record.setTitle(contentData.getTitle());
                    record.setContent(contentData.getContent());
                    
                    createDocumentContent.add(record);
                } else {
                    UUID retrievedId = rs.getObject("id", UUID.class);
                    Long retrievedVersion = rs.getLong("version");
                    String retrievedTitle = rs.getString("title");
                    String retrievedContent = rs.getString("content");
                    boolean updated = rs.getBoolean("updated");

                    UuidIdentifier retrievedIdentifier = new UuidIdentifier(retrievedId, retrievedVersion);
                    StandardDocumentContentEntry standardDocumentContent = new StandardDocumentContentEntry(retrievedTitle, retrievedContent);
                    
                    if (!updated) {
                        conflictDocumentContent.put(retrievedIdentifier, standardDocumentContent);
                    }
                }
            }
        }

        return new UpdateOrReturnExistingReturnValues(conflictDocumentContent, createDocumentContent);
    }

    private void insertDocumentContent(UuidIdentifier documentId, Vector<StandardDocumentContentRecord> createDocumentContent, Connection connection) throws SQLException {
        // TODO: this is repeated
        String insertDocumentContentSql = 
            "INSERT INTO document_content(id, document_id, title, content)"
            + " VALUES (?, ?, ?, ?)"
            + " ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = connection.prepareStatement(insertDocumentContentSql)){
            for (StandardDocumentContentRecord entry : createDocumentContent) {
                stmt.setObject(1, entry.getId(), Types.OTHER);
                stmt.setObject(2, documentId.value(), Types.OTHER);
                stmt.setString(3, entry.getTitle());
                stmt.setString(4, entry.getContent());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }

    private void deleteExcluding(StandardDocumentContent content, UuidIdentifier documentIdentifier, Connection connection) throws SQLException {
        UUID[] idsPresent = new Vector<>(content.getContent().keySet())
            .stream()
            .map(element -> element.value())
            .toArray(UUID[]::new);

        String placeholders = content.getContent().keySet().stream()
            .map(id -> "?")
            .collect(Collectors.joining(", "));

        String deleteIdsNotPresentSql =
            "DELETE FROM document_content WHERE document_id = ? AND id NOT IN (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(deleteIdsNotPresentSql)) {
            ps.setObject(1, documentIdentifier.value(), Types.OTHER);
            int i = 2;
            for (UUID id : idsPresent) {
                ps.setObject(i++, id, Types.OTHER);
            }
            ps.executeUpdate();
        }
    }

    public Optional<HashMap<UuidIdentifier, StandardDocumentContentEntry>> upsertDocument(Document<UUID, UuidIdentifier, StandardDocumentContent> document) throws SQLException {
        HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent = null;

        Connection connection = null;
        try {
            connection = db.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            boolean inserted = insertDocumentIfNew(document, connection);
            if (!inserted && document.getContent().isPresent()) {
                UpdateOrReturnExistingReturnValues updatedReturn = updateOrReturnExisting(document, connection);

                conflictDocumentContent = updatedReturn.conflictDocumentContent;
                insertDocumentContent(document.getId(), updatedReturn.createDocumentContent, connection);

                StandardDocumentContent content = document.getContent().get();
                deleteExcluding(content, document.getId(), connection);
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }


        return Optional.ofNullable(conflictDocumentContent);
    }

    public void unlinkDocument(UuidIdentifier document, UuidIdentifier linkDocument, Connection connection) {
        // TODO Auto-generated method stub
        
    }
}
