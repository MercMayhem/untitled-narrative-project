package com.untitled.project.data;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;

import com.untitled.project.core.Document;
import com.untitled.project.data.update.DeleteStandardDocumentContentResult;
import com.untitled.project.data.update.InsertStandardDocumentContentResult;
import com.untitled.project.data.update.StandardDocumentUpdateResult;
import com.untitled.project.data.update.UpdateStandardDocumentContentResult;
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

    private InsertStandardDocumentContentResult insertDocumentContent(Vector<StandardDocumentContentRecord> records, Connection connection) throws SQLException {
        String sql =
            "INSERT INTO document_content (" +
            "id, document_id, title, content, created_at, updated_at" +
            ") " +
            "SELECT * FROM UNNEST(" +
            "?::uuid[], " +
            "?::uuid[], " +
            "?::text[], " +
            "?::text[], " +
            "?::timestamptz[], " +
            "?::timestamptz[]" +
            ") " +
            "ON CONFLICT DO NOTHING " +
            "RETURNING id";

        UUID[] ids = new UUID[records.size()];
        UUID[] documentIds = new UUID[records.size()];
        String[] titles = new String[records.size()];
        String[] contents = new String[records.size()];
        Timestamp[] createdAts = new Timestamp[records.size()];
        Timestamp[] updatedAts = new Timestamp[records.size()];

        for (int i = 0; i < records.size(); i++) {
            StandardDocumentContentRecord r = records.get(i);
            ids[i] = r.getId();
            documentIds[i] = r.getDocumentId();
            titles[i] = r.getTitle();
            contents[i] = r.getContent();
            createdAts[i] = r.getCreatedAt().map(Timestamp::from).orElse(null);
            updatedAts[i] = r.getUpdatedAt().map(Timestamp::from).orElse(null);
        }

        Vector<StandardDocumentContentRecord> created = new Vector<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setArray(1, connection.createArrayOf("uuid", ids));
            ps.setArray(2, connection.createArrayOf("uuid", documentIds));
            ps.setArray(3, connection.createArrayOf("text", titles));
            ps.setArray(4, connection.createArrayOf("text", contents));
            ps.setArray(5, connection.createArrayOf("timestamptz", createdAts));
            ps.setArray(6, connection.createArrayOf("timestamptz", updatedAts));

            Map<UUID, StandardDocumentContentRecord> byId = new HashMap<>(records.size());
            for (StandardDocumentContentRecord r : records) {
                byId.put(r.getId(), r);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = rs.getObject("id", UUID.class);
                    created.add(byId.get(id));
                }
            }
        }

        return new InsertStandardDocumentContentResult(created.size() != records.size(), created);
    }
    private UpdateStandardDocumentContentResult updateDocumentContent(Vector<StandardDocumentContentRecord> records,Connection connection) throws SQLException {

        if (records.isEmpty()) {
            return new UpdateStandardDocumentContentResult(false, new Vector<>());
        }

        String sql =
            "UPDATE document_content dc SET " +
            "title = u.title, " +
            "content = u.content, " +
            "updated_at = u.updated_at, " +
            "version = dc.version + 1 " +
            "FROM ( " +
            "SELECT * FROM UNNEST( " +
            "?::uuid[], " +
            "?::text[], " +
            "?::text[], " +
            "?::timestamptz[], " +
            "?::bigint[] " +
            ") ) AS u(id, title, content, updated_at, version) " +
            "WHERE dc.id = u.id AND dc.version = u.version " +
            "RETURNING dc.id";

        UUID[] ids = new UUID[records.size()];
        String[] titles = new String[records.size()];
        String[] contents = new String[records.size()];
        Timestamp[] updatedAts = new Timestamp[records.size()];
        Long[] versions = new Long[records.size()];

        for (int i = 0; i < records.size(); i++) {
            StandardDocumentContentRecord r = records.get(i);
            ids[i] = r.getId();
            titles[i] = r.getTitle();
            contents[i] = r.getContent();
            updatedAts[i] = r.getUpdatedAt().map(Timestamp::from).orElse(null);
            versions[i] = r.getVersion();
        }

        Map<UUID, StandardDocumentContentRecord> byId = new HashMap<>(records.size());
        for (StandardDocumentContentRecord r : records) {
            byId.put(r.getId(), r);
        }

        Vector<StandardDocumentContentRecord> updated = new Vector<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setArray(1, connection.createArrayOf("uuid", ids));
            ps.setArray(2, connection.createArrayOf("text", titles));
            ps.setArray(3, connection.createArrayOf("text", contents));
            ps.setArray(4, connection.createArrayOf("timestamptz", updatedAts));
            ps.setArray(5, connection.createArrayOf("bigint", versions));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID id = rs.getObject("id", UUID.class);
                    updated.add(byId.get(id));
                }
            }
        }

        boolean optimisticLockingError =
            updated.size() != records.size();

        return new UpdateStandardDocumentContentResult(
            optimisticLockingError,
            updated
        );
    }


    private DeleteStandardDocumentContentResult deleteContentById(UuidIdentifier documentContentIdentifier, UuidIdentifier documentIdentifier, Connection connection) throws SQLException {
        String deleteContentSql =
            "DELETE FROM document_content WHERE document_id = ? AND id = ? AND version = ?";

        try (PreparedStatement ps = connection.prepareStatement(deleteContentSql)) {
            ps.setObject(1, documentIdentifier.value(), Types.OTHER);
            ps.setObject(2, documentContentIdentifier.value(), Types.OTHER);
            ps.setLong(4, documentContentIdentifier.getVersion());
            int deletedCount = ps.executeUpdate();

            return new DeleteStandardDocumentContentResult(deletedCount == 0);
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

    public StandardDocumentUpdateResult updateDocument(StandardDocumentUpdate documentUpdate) throws SQLException {

        Connection connection = null;
        try {
            connection = StandardDocumentRepo.ds().getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            StandardDocumentUpdateResult res = null;
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

                    InsertStandardDocumentContentResult result = insertDocumentContent(contentRecords, connection);
                    res = new StandardDocumentUpdateResult(result);
                }

                case UpdateDocumentContent updateDocumentContent -> {
                    // TODO: use converter class to convert from StandardDocumentContent to Vector<StandardDocumentContentRecord>
                    Vector<StandardDocumentContentRecord> contentRecords = updateDocumentContent
                        .updateDocumentContent()
                        .getContent()
                        .entrySet()
                        .stream()
                        .map(entry -> {
                            return new StandardDocumentContentRecord(entry.getKey(), entry.getValue(), documentUpdate.getDocumentIdentifier());
                        })
                        .collect(Collectors.toCollection(Vector::new));

                    UpdateStandardDocumentContentResult result = updateDocumentContent(contentRecords, connection);
                    res = new StandardDocumentUpdateResult(result);
                }

                case DeleteDocumentContent deleteDocumentContent -> {
                    DeleteStandardDocumentContentResult result = deleteContentById(deleteDocumentContent.id(), documentUpdate.getDocumentIdentifier(), connection);
                    res = new StandardDocumentUpdateResult(result);
                }
            }

            connection.commit();
            return res;
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
