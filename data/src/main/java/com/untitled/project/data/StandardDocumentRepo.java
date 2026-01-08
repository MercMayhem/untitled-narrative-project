package com.untitled.project.data;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import com.untitled.project.core.Document;
import com.untitled.project.models.document.StandardDocumentContentEntry;
import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.document.StandardDocumentContent;
import com.untitled.project.models.document.UuidIdentifier;
import com.untitled.project.models.document.update.DeleteDocumentContent;
import com.untitled.project.models.document.update.InsertDocumentContent;
import com.untitled.project.models.document.update.StandardDocumentUpdate;
import com.untitled.project.models.document.update.UpdateDocumentContent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class StandardDocumentRepo implements Closeable {
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

    private static HikariDataSource ds() {
        return db;
    }

    // public void deleteDocument(UuidIdentifier id, Connection connection) {
    //     // TODO Auto-generated method stub
        
    // }

    public Optional<StandardDocument> getDocumentById(UuidIdentifier id) throws SQLException{
        Connection connection = null;
        try {
            connection = StandardDocumentRepo.ds().getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            Optional<StandardDocumentRecord> documentRecordOptional = StandardDocumentRecord.get(id.value(), connection);
            if (documentRecordOptional.isEmpty()) {
                return Optional.ofNullable(null);
            }

            StandardDocumentRecord documentRecord = documentRecordOptional.get();
            Vector<StandardDocumentContentRecord> documentContentRecords = StandardDocumentContentRecord.get(id.value(), connection);

            connection.commit();
            StandardDocument document = new RecordToStandardDocumentConverter(documentRecord, documentContentRecords).toStandardDocument();
            return Optional.ofNullable(document);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.close();
        }
    }

    // public void linkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
    //     // TODO Auto-generated method stub
        
    // }

    private void insertDocument(Document<UUID, UuidIdentifier, StandardDocumentContent> document, Connection connection) throws SQLException {
        // TODO: implement using the converter class
        String insertDocumentSql = 
            "INSERT INTO document(id) VALUES (?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(insertDocumentSql)) {
            stmt.setObject(1, document.getId().value(), Types.OTHER);
            stmt.executeUpdate();
        }

        if (document.getContent().isPresent()) {
            // TODO: use converter class to convert from StandardDocumentContent to Vector<StandardDocumentContentRecord>
            Vector<StandardDocumentContentRecord> contentRecords = document.getContent().get()
                .getContent()
                .entrySet()
                .stream()
                .map(entry -> {
                    return new StandardDocumentContentRecord(entry.getKey(), entry.getValue(), document.getId());
                })
                .collect(Collectors.toCollection(Vector::new));

            insertDocumentContent(contentRecords, connection);
        }
    }

    // private class UpdateOrReturnExistingReturnValues {
    //     public HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent;
    //     public Vector<StandardDocumentContentRecord> createDocumentContent;
        
    //     public UpdateOrReturnExistingReturnValues(
    //         HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent,
    //         Vector<StandardDocumentContentRecord> createDocumentContent
    //     ) {
    //         this.conflictDocumentContent = conflictDocumentContent;
    //         this.createDocumentContent = createDocumentContent;
    //     } 
    // }

    // private UpdateOrReturnExistingReturnValues updateOrReturnExisting(Document<UUID, UuidIdentifier, StandardDocumentContent> document, Connection connection) throws SQLException {
    //     HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent = new HashMap<>();
    //     Vector<StandardDocumentContentRecord> createDocumentContent = new Vector<>();
    //     StandardDocumentContent content = document.getContent().get();

    //     String updateDocumentContentSql = 
    //         " UPDATE document_content"
    //         + " SET"
    //         + " version = version + 1,"
    //         + " title = ?,"
    //         + " content = ?,"
    //         + " updated_at =  now()"
    //         + " WHERE id = ? AND version = ? - 1"
    //         + " RETURNING *, true as updated";

    //     String updateOrReturnExistingVersionSql = 
    //         "WITH updated AS (" + updateDocumentContentSql + ")"
    //         + " SELECT *"
    //         + " FROM updated"
    //         + " UNION ALL"
    //         + " SELECT *, false as updated"
    //         + " FROM document_content"
    //         + " WHERE id = ?"
    //         + " AND NOT EXISTS(SELECT 1 FROM updated)";

    //     try (PreparedStatement stmt = connection.prepareStatement(updateOrReturnExistingVersionSql)){
    //         for (Map.Entry<UuidIdentifier, StandardDocumentContentEntry> entry : content.getContent().entrySet()) {
    //             UuidIdentifier id = entry.getKey();
    //             StandardDocumentContentEntry contentData = entry.getValue();
    //             stmt.setString(1, contentData.getTitle());
    //             stmt.setString(2, contentData.getContent());
    //             stmt.setObject(3, id.value(), Types.OTHER);
    //             stmt.setLong(4, id.getVersion());
    //             stmt.setObject(5, id.value(), Types.OTHER);

    //             ResultSet rs = stmt.executeQuery();
    //             if (!rs.next()) {
    //                 StandardDocumentContentRecord record = new StandardDocumentContentRecord();

    //                 record.setId(id.value());
    //                 record.setDocumentId(document.getId().value());
    //                 record.setTitle(contentData.getTitle());
    //                 record.setContent(contentData.getContent());
                    
    //                 createDocumentContent.add(record);
    //             } else {
    //                 UUID retrievedId = rs.getObject("id", UUID.class);
    //                 Long retrievedVersion = rs.getLong("version");
    //                 String retrievedTitle = rs.getString("title");
    //                 String retrievedContent = rs.getString("content");
    //                 boolean updated = rs.getBoolean("updated");

    //                 UuidIdentifier retrievedIdentifier = new UuidIdentifier(retrievedId, retrievedVersion);
    //                 StandardDocumentContentEntry standardDocumentContent = new StandardDocumentContentEntry(retrievedTitle, retrievedContent);
                    
    //                 if (!updated && retrievedVersion >= id.getVersion()) {
    //                     conflictDocumentContent.put(retrievedIdentifier, standardDocumentContent);
    //                 }
    //             }
    //         }
    //     }

    //     return new UpdateOrReturnExistingReturnValues(conflictDocumentContent, createDocumentContent);
    // }

    private void insertDocumentContent(Vector<StandardDocumentContentRecord> createDocumentContent, Connection connection) throws SQLException {
        String insertDocumentContentSql = 
            "INSERT INTO document_content(id, document_id, title, content, created_at, updated_at)"
            + " VALUES (?, ?, ?, ?, ?, ?)"
            + " ON CONFLICT DO NOTHING";

        try (PreparedStatement stmt = connection.prepareStatement(insertDocumentContentSql)){
            for (StandardDocumentContentRecord entry : createDocumentContent) {
                stmt.setObject(1, entry.getId(), Types.OTHER);
                stmt.setObject(2, entry.getDocumentId(), Types.OTHER);
                stmt.setString(3, entry.getTitle());
                stmt.setString(4, entry.getContent());
                stmt.setObject(5, entry.getCreatedAt());
                stmt.setObject(6, entry.getUpdatedAt());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }

    private void deleteContentById(UuidIdentifier documentContentIdentifier, UuidIdentifier documentIdentifier, Connection connection) throws SQLException {
        String deleteContentSql =
            "DELETE FROM document_content WHERE document_id = ? AND id = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteContentSql)) {
            ps.setObject(1, documentIdentifier.value(), Types.OTHER);
            ps.setObject(2, documentContentIdentifier.value(), Types.OTHER);
            ps.executeUpdate();
        }
    }

    public Optional<HashMap<UuidIdentifier, StandardDocumentContentEntry>> insertDocument(Document<UUID, UuidIdentifier, StandardDocumentContent> document) throws SQLException {
        HashMap<UuidIdentifier, StandardDocumentContentEntry> conflictDocumentContent = null;

        Connection connection = null;
        try {
            connection = StandardDocumentRepo.ds().getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            insertDocument(document, connection);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }


        return Optional.ofNullable(conflictDocumentContent);
    }

    public void updateDocument(StandardDocumentUpdate documentUpdate) throws SQLException {

        Connection connection = null;
        try {
            connection = StandardDocumentRepo.ds().getConnection();
            connection.setAutoCommit(false);

            switch (documentUpdate.getUpdateType()) {
                case InsertDocumentContent insertContent -> {
                    // TODO: use converter class to convert from StandardDocumentContent to Vector<StandardDocumentContentRecord>
                    Vector<StandardDocumentContentRecord> contentRecords = insertContent
                        .insertStandardDocumentContent()
                        .getContent()
                        .entrySet()
                        .stream()
                        .map(entry -> {
                            return new StandardDocumentContentRecord(entry.getKey(), entry.getValue(), documentUpdate.getDocumentIdentifier());
                        })
                        .collect(Collectors.toCollection(Vector::new));

                    insertDocumentContent(contentRecords, connection);
                }

                case UpdateDocumentContent updateDocumentContent -> {
                    
                }

                case DeleteDocumentContent deleteDocumentContent -> {
                    deleteContentById(deleteDocumentContent.id(), documentUpdate.getDocumentIdentifier(), connection);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    public void unlinkDocument(UuidIdentifier document, UuidIdentifier linkDocument, Connection connection) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void close() {
        StandardDocumentRepo.ds().close();
    }
}
