package com.untitled.project.data;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.untitled.project.core.Document;
import com.untitled.project.core.identifier.DocumentIdentifier;
import com.untitled.project.core.repo.DocumentRepo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class StandardDocumentRepo<U, V extends DocumentIdentifier<U>> implements DocumentRepo<U, V> {
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

    @Override
    public void deleteDocument(V id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Document<U, V> getDocumentCollectionById(V id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void linkDocument(V document, V linkDocument) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void upsertDocument(Document<U, V> document) throws SQLException {
        String insertDocumentSql = 
            "INSERT INTO document(content, internal_id) VALUES (?, ?)"
            + " ON CONFLICT DO UPDATE SET content = EXCLUDED.content";

        try (PreparedStatement stmt = StandardDocumentRepo.ds().getConnection().prepareStatement(insertDocumentSql)) {
            stmt.setString(1, document.getContent().orElse(null).rawString());
            stmt.setString(2, document.getId().rawString());
            stmt.executeUpdate();
        }
    }

    @Override
    public void unlinkDocument(V document, V linkDocument) {
        // TODO Auto-generated method stub
        
    }
}
