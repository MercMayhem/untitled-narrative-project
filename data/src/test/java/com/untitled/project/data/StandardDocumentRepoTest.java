package com.untitled.project.data;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.document.UuidIdentifier;
import com.zaxxer.hikari.HikariConfig;

@Testcontainers
class StandardDocumentRepoTest {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    private static StandardDocumentRepo repo;
    
    @BeforeAll
    static void setupDatabase() throws SQLException {
        // Configure HikariCP with Testcontainers connection info
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(5);
        
        // Initialize repository
        repo = new StandardDocumentRepo(config);
        
        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }
    
    // TODO: Improve this implementation of clearing the tables
    @BeforeEach
    void cleanDatabase() throws SQLException {
        // Clear table before each test
        try (Connection conn = repo.ds().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE document CASCADE");
        }
    }
    
    @AfterAll
    static void tearDown() {
        if (repo != null && repo.ds() != null) {
            repo.ds().close();
        }
    }
    
    @Test
    void testUpsertNewDocument() throws SQLException {
        // Arrange
        StandardDocument document = new StandardDocument();
        UuidIdentifier identifier = document.getId();
        
        // Act
        repo.upsertDocument(document);
        
        // Assert
        // StandardDocument retrieved = repo.getDocumentById(identifier);
        // assertNotNull(retrieved);
        // assertEquals(identifier.value(), retrieved.getId().value());
        // assertTrue(retrieved.getContent().isPresent());
    }
    
    // @Test
    // void testUpsertExistingDocument() throws SQLException {
    //     // Arrange - Insert initial document
    //     UUID docId = UUID.randomUUID();
    //     UuidIdentifier identifier = new UuidIdentifier(docId);
    //     StandardDocumentContent originalContent = new StandardDocumentContent("Original content");
    //     StandardDocument originalDoc = new StandardDocument(
    //         docId,
    //         identifier,
    //         Optional.of(originalContent),
    //         null,
    //         null
    //     );
    //     repo.upsertDocument(originalDoc);
        
    //     // Act - Update with new content
    //     StandardDocumentContent updatedContent = new StandardDocumentContent("Updated content");
    //     StandardDocument updatedDoc = new StandardDocument(
    //         docId,
    //         identifier,
    //         Optional.of(updatedContent),
    //         null,
    //         null
    //     );
    //     repo.upsertDocument(updatedDoc);
        
    //     // Assert
    //     StandardDocument retrieved = repo.getDocumentById(identifier);
    //     assertNotNull(retrieved);
    //     assertEquals(docId, retrieved.getId());
    //     assertTrue(retrieved.getContent().isPresent());
    //     assertEquals("Updated content", retrieved.getContent().get().rawString());
    // }
    
    // @Test
    // void testUpsertDocumentWithNullContent() throws SQLException {
    //     // Arrange
    //     UUID docId = UUID.randomUUID();
    //     UuidIdentifier identifier = new UuidIdentifier(docId);
    //     StandardDocument document = new StandardDocument(
    //         docId,
    //         identifier,
    //         Optional.empty(),
    //         null,
    //         null
    //     );
        
    //     // Act
    //     repo.upsertDocument(document);
        
    //     // Assert
    //     StandardDocument retrieved = repo.getDocumentById(identifier);
    //     assertNotNull(retrieved);
    //     assertEquals(docId, retrieved.getId());
    //     // Depending on implementation, this might be empty or contain null
    // }
    
    // @Test
    // void testGetDocumentById() throws SQLException {
    //     // Arrange
    //     UUID docId = UUID.randomUUID();
    //     UuidIdentifier identifier = new UuidIdentifier(docId);
    //     StandardDocumentContent content = new StandardDocumentContent("Get test content");
    //     StandardDocument document = new StandardDocument(
    //         docId,
    //         identifier,
    //         Optional.of(content),
    //         null,
    //         null
    //     );
    //     repo.upsertDocument(document);
        
    //     // Act
    //     StandardDocument retrieved = repo.getDocumentById(identifier);
        
    //     // Assert
    //     assertNotNull(retrieved);
    //     assertEquals(docId, retrieved.getId());
    //     assertTrue(retrieved.getContent().isPresent());
    //     assertEquals("Get test content", retrieved.getContent().get().rawString());
    // }
    
    // @Test
    // void testGetDocumentByIdNonExistent() {
    //     // Arrange
    //     UuidIdentifier nonExistentId = new UuidIdentifier(UUID.randomUUID());
        
    //     // Act & Assert
    //     assertThrows(SQLException.class, () -> {
    //         repo.getDocumentById(nonExistentId);
    //     });
    // }
    
    // @Test
    // void testMultipleUpserts() throws SQLException {
    //     // Arrange & Act - Insert multiple documents
    //     UUID docId1 = UUID.randomUUID();
    //     UUID docId2 = UUID.randomUUID();
        
    //     repo.upsertDocument(new StandardDocument(
    //         docId1,
    //         new UuidIdentifier(docId1),
    //         Optional.of(new StandardDocumentContent("Content 1")),
    //         null,
    //         null
    //     ));
        
    //     repo.upsertDocument(new StandardDocument(
    //         docId2,
    //         new UuidIdentifier(docId2),
    //         Optional.of(new StandardDocumentContent("Content 2")),
    //         null,
    //         null
    //     ));
        
    //     // Assert - Both documents exist independently
    //     StandardDocument doc1 = repo.getDocumentById(new UuidIdentifier(docId1));
    //     StandardDocument doc2 = repo.getDocumentById(new UuidIdentifier(docId2));
        
    //     assertEquals("Content 1", doc1.getContent().get().rawString());
    //     assertEquals("Content 2", doc2.getContent().get().rawString());
    // }
}