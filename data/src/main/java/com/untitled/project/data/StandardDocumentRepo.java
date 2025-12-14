package com.untitled.project.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.untitled.project.core.Document;
import com.untitled.project.core.repo.DocumentRepo;
import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.document.StandardDocumentContent;
import com.untitled.project.models.document.UuidIdentifier;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class StandardDocumentRepo implements DocumentRepo<UUID, UuidIdentifier, StandardDocumentContent> {
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
    public void deleteDocument(UuidIdentifier id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public StandardDocument getDocumentById(UuidIdentifier id) throws SQLException{
        String getDocumentSql = "SELECT * FROM document WHERE internal_id = ?";

        StandardDocumentRecord documentModel = new StandardDocumentRecord();
        try (Connection conn = StandardDocumentRepo.ds().getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(getDocumentSql);
            pstmt.setString(1, id.rawString());

            ResultSet rs = pstmt.executeQuery();        

            documentModel.setId(rs.getObject("id", UUID.class));
            documentModel.setInternalId(rs.getString("internal_id"));
            documentModel.setContent(rs.getString("content"));
            documentModel.setCreatedAt(rs.getObject("created_at", ZonedDateTime.class));
            documentModel.setUpdatedAt(rs.getObject("updated_at", ZonedDateTime.class));
        }

        return documentModel.toStandardDocument();
    }

    @Override
    public void linkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void upsertDocument(Document<UUID, UuidIdentifier, StandardDocumentContent> document) throws SQLException {
        String insertDocumentSql = 
            "INSERT INTO document(content, internal_id) VALUES (?, ?)"
            + " ON CONFLICT DO UPDATE SET content = EXCLUDED.content";

        try (PreparedStatement stmt = StandardDocumentRepo.ds().getConnection().prepareStatement(insertDocumentSql)) {
            stmt.setString(1, document.getContent().map(StandardDocumentContent::rawString).orElse(null));
            stmt.setString(2, document.getId().rawString());
            stmt.executeUpdate();
        }
    }

    @Override
    public void unlinkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
        // TODO Auto-generated method stub
        
    }
}
