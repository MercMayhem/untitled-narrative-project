package com.untitled.project.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
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

    static HikariDataSource ds() {
        return db;
    }

    @Override
    public void deleteDocument(UuidIdentifier id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public StandardDocument getDocumentById(UuidIdentifier id) throws SQLException{
        String getDocumentSql = "SELECT * FROM document WHERE id = ?";
        StandardDocumentRecord documentModel = new StandardDocumentRecord();
        
        System.out.println(">>> getDocumentById called");
        System.out.println("Searching for ID: " + id.value());
        
        try (Connection conn = StandardDocumentRepo.ds().getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(getDocumentSql);
            pstmt.setObject(1, id.value(), Types.OTHER);
            
            System.out.println("Executing query: " + getDocumentSql);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("!!! Document not found for ID: " + id.value());
                throw new SQLException("Document not found");
            }
            
            System.out.println("Document found! Retrieving fields...");
            
            UUID retrievedId = rs.getObject("id", UUID.class);
            String content = rs.getString("content");
            Instant createdAt = rs.getTimestamp("created_at").toInstant();
            Instant updatedAt = rs.getTimestamp("updated_at").toInstant();
            
            System.out.println("Retrieved ID: " + retrievedId);
            System.out.println("Retrieved content: " + content);
            System.out.println("Retrieved created_at: " + createdAt);
            System.out.println("Retrieved updated_at: " + updatedAt);
            
            documentModel.setId(retrievedId);
            documentModel.setContent(content);
            documentModel.setCreatedAt(createdAt);
            documentModel.setUpdatedAt(updatedAt);
        }
        
        System.out.println("<<< getDocumentById complete");
        return documentModel.toStandardDocument();
    }

    @Override
    public void linkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void upsertDocument(Document<UUID, UuidIdentifier, StandardDocumentContent> document) throws SQLException {
        String insertDocumentSql = 
            "INSERT INTO document(content, id) VALUES (?, ?)"
            + " ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content";

        try (PreparedStatement stmt = StandardDocumentRepo.ds().getConnection().prepareStatement(insertDocumentSql)) {
            stmt.setString(1, document.getContent().map(StandardDocumentContent::rawString).orElse(null));
            stmt.setObject(2, document.getId().value(), Types.OTHER);
            stmt.executeUpdate();
        }
    }

    @Override
    public void unlinkDocument(UuidIdentifier document, UuidIdentifier linkDocument) {
        // TODO Auto-generated method stub
        
    }
}
