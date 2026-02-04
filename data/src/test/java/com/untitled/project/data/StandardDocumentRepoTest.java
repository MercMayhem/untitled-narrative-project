package com.untitled.project.data;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Optional;

import com.untitled.project.data.update.DeleteStandardDocumentContentResult;
import com.untitled.project.data.update.InsertStandardDocumentContentResult;
import com.untitled.project.data.update.StandardDocumentUpdateResult;
import com.untitled.project.data.update.UpdateStandardDocumentContentResult;
import com.untitled.project.models.document.update.DeleteDocumentContent;
import com.untitled.project.models.document.update.InsertDocumentContent;
import com.untitled.project.models.document.update.StandardDocumentUpdate;
import com.untitled.project.models.document.update.UpdateDocumentContent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.document.StandardDocumentContent;
import com.untitled.project.models.document.StandardDocumentContentEntry;
import com.untitled.project.models.document.UuidIdentifier;
import com.untitled.project.models.document.UuidIdentifierGenerator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Testcontainers
public class StandardDocumentRepoTest {
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    private static StandardDocumentRepo repo;
    private static HikariDataSource testPool;
    
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
        testPool = new HikariDataSource(config);
        
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
        try (Connection conn = testPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE document CASCADE");
        }
    }
    
    @AfterAll
    static void tearDown() {
        if (testPool != null) {
            testPool.close();
        }

        repo.close();
    }

    @Test
    void testInsertNewDocument() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content.put(identifierGenerator.generateUnique(), new StandardDocumentContentEntry("title1", "content1", new BigDecimal(1)));
        content.put(identifierGenerator.generateUnique(), new StandardDocumentContentEntry("title2", "content2", new BigDecimal(2)));

        StandardDocument document = new StandardDocument(content);
        UuidIdentifier identifier = document.getId();

        // Act
        repo.insertDocument(document);

        // Assert
        Optional<StandardDocument> retrievedOptional = repo.getDocumentById(identifier);
        assert(retrievedOptional.isPresent());
        StandardDocument retrieved = retrievedOptional.get();
        assertEquals(identifier.value(), retrieved.getId().value());
        assertTrue(retrieved.getContent().isPresent());

        StandardDocumentContent retrievedContent = retrieved.getContent().get();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> retrievedContentEntries = retrievedContent.getContent();

        System.out.println("Retrieved: " + retrievedContentEntries);
        System.out.println("Created: " + content);

        assert(content.equals(retrievedContentEntries));
    }

    @Test
    void testUpdateDocumentContent() throws SQLException {
        // Arrange - Create a document with content
        HashMap<UuidIdentifier, StandardDocumentContentEntry> initialContent = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        UuidIdentifier contentId1 = identifierGenerator.generateUnique();
        UuidIdentifier contentId2 = identifierGenerator.generateUnique();

        initialContent.put(contentId1,
                new StandardDocumentContentEntry("original title 1", "original content 1", new BigDecimal(1)));
        initialContent.put(contentId2,
                new StandardDocumentContentEntry("original title 2", "original content 2", new BigDecimal(2)));

        StandardDocument document = new StandardDocument(initialContent);
        UuidIdentifier documentId = document.getId();
        repo.insertDocument(document);

        // Retrieve to get current version
        StandardDocument current = repo.getDocumentById(documentId).get();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> currentEntries =
                current.getContent().get().getContent();

        // Arrange - Prepare updated content
        HashMap<UuidIdentifier, StandardDocumentContentEntry> updatedContent = new HashMap<>();
        UuidIdentifier updatedContentId1 = new UuidIdentifier(contentId1.value(), contentId1.getVersion() + 1);
        updatedContent.put(updatedContentId1,
                new StandardDocumentContentEntry("updated title 1", "updated content 1", new BigDecimal(1)));

        UpdateDocumentContent updateContent = new UpdateDocumentContent(
                new StandardDocumentContent(updatedContent)
        );

        StandardDocumentUpdate update = new StandardDocumentUpdate(updateContent, documentId);

        // Act
        StandardDocumentUpdateResult result = repo.updateDocument(update);

        // Assert
        assertFalse(((UpdateStandardDocumentContentResult) result.updateDocumentContentResult.get()).optimisticLockingError());

        Optional<StandardDocument> retrievedOptional = repo.getDocumentById(documentId);
        assertTrue(retrievedOptional.isPresent());
        StandardDocument retrieved = retrievedOptional.get();

        assertTrue(retrieved.getContent().isPresent());
        HashMap<UuidIdentifier, StandardDocumentContentEntry> retrievedEntries =
                retrieved.getContent().get().getContent();

        // Verify updated content
        StandardDocumentContentEntry updatedEntry = retrievedEntries.get(contentId1);
        assertEquals("updated title 1", updatedEntry.getTitle());
        assertEquals("updated content 1", updatedEntry.getContent());

        // Verify unchanged content still exists
        assertTrue(retrievedEntries.containsKey(contentId2));
        StandardDocumentContentEntry unchangedEntry = retrievedEntries.get(contentId2);
        assertEquals("original title 2", unchangedEntry.getTitle());
        assertEquals("original content 2", unchangedEntry.getContent());
    }
    @Test
    void testInsertDocumentContent() throws SQLException {
        // Arrange - Create a document first
        HashMap<UuidIdentifier, StandardDocumentContentEntry> initialContent = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        initialContent.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title1", "content1", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(initialContent);
        UuidIdentifier documentId = document.getId();
        repo.insertDocument(document);

        // Arrange - Prepare new content to insert
        HashMap<UuidIdentifier, StandardDocumentContentEntry> newContent = new HashMap<>();
        newContent.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title2", "content2", new BigDecimal(2)));
        newContent.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title3", "content3", new BigDecimal(3)));

        InsertDocumentContent insertUpdate = new InsertDocumentContent(
                new StandardDocumentContent(newContent)
        );
        StandardDocumentUpdate update = new StandardDocumentUpdate(insertUpdate, documentId);

        // Act
        StandardDocumentUpdateResult result = repo.updateDocument(update);

        // Assert
        assertFalse(((InsertStandardDocumentContentResult) result.updateDocumentContentResult.get()).optimisticLockingError());

        Optional<StandardDocument> retrievedOptional = repo.getDocumentById(documentId);
        assertTrue(retrievedOptional.isPresent());
        StandardDocument retrieved = retrievedOptional.get();

        assertTrue(retrieved.getContent().isPresent());
        HashMap<UuidIdentifier, StandardDocumentContentEntry> retrievedEntries =
                retrieved.getContent().get().getContent();

        // Should have 3 total entries (1 initial + 2 new)
        assertEquals(3, retrievedEntries.size());

        // Verify new content exists
        newContent.forEach((key, value) -> {
            assertTrue(retrievedEntries.containsKey(key));
            assertEquals(value, retrievedEntries.get(key));
        });
    }
    @Test
    void testDeleteDocumentContent() throws SQLException {
        // Arrange - Create a document with multiple content entries
        HashMap<UuidIdentifier, StandardDocumentContentEntry> initialContent = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        UuidIdentifier contentId1 = identifierGenerator.generateUnique();
        UuidIdentifier contentId2 = identifierGenerator.generateUnique();

        initialContent.put(contentId1,
                new StandardDocumentContentEntry("title1", "content1", new BigDecimal(1)));
        initialContent.put(contentId2,
                new StandardDocumentContentEntry("title2", "content2", new BigDecimal(2)));

        StandardDocument document = new StandardDocument(initialContent);
        UuidIdentifier documentId = document.getId();
        repo.insertDocument(document);

        // Retrieve to get current version
        StandardDocument current = repo.getDocumentById(documentId).get();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> currentEntries =
                current.getContent().get().getContent();

        // Find the actual identifier with version for contentId1
        UuidIdentifier actualContentId1 = currentEntries.keySet().stream()
                .filter(id -> id.value().equals(contentId1.value()))
                .findFirst()
                .orElseThrow();

        // Arrange - Prepare delete request with incremented version
        UuidIdentifier deleteContentId = new UuidIdentifier(actualContentId1.value(), actualContentId1.getVersion() + 1);
        DeleteDocumentContent deleteContent = new DeleteDocumentContent(deleteContentId);
        StandardDocumentUpdate update = new StandardDocumentUpdate(deleteContent, documentId);

        // Act
        StandardDocumentUpdateResult result = repo.updateDocument(update);

        // Assert
        assertFalse(((DeleteStandardDocumentContentResult) result.updateDocumentContentResult.get()).optimisticLockingError());

        Optional<StandardDocument> retrievedOptional = repo.getDocumentById(documentId);
        assertTrue(retrievedOptional.isPresent());
        StandardDocument retrieved = retrievedOptional.get();

        assertTrue(retrieved.getContent().isPresent());
        HashMap<UuidIdentifier, StandardDocumentContentEntry> retrievedEntries =
                retrieved.getContent().get().getContent();

        // Should have 1 entry remaining
        assertEquals(1, retrievedEntries.size());

        // Verify deleted content is gone (check by UUID value, not identifier object)
        assertFalse(retrievedEntries.keySet().stream()
                .anyMatch(id -> id.value().equals(contentId1.value())));

        // Verify other content still exists
        assertTrue(retrievedEntries.keySet().stream()
                .anyMatch(id -> id.value().equals(contentId2.value())));
    }

    @Test
    void testDeleteDocumentContentWithOptimisticLockingError() throws SQLException {
        // Arrange - Create a document with content
        HashMap<UuidIdentifier, StandardDocumentContentEntry> initialContent = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        UuidIdentifier contentId = identifierGenerator.generateUnique();
        initialContent.put(contentId,
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(initialContent);
        UuidIdentifier documentId = document.getId();
        repo.insertDocument(document);

        // Arrange - Create delete with wrong version
        UuidIdentifier staleContentId = new UuidIdentifier(contentId.value(), 999L);
        DeleteDocumentContent deleteContent = new DeleteDocumentContent(staleContentId);
        StandardDocumentUpdate update = new StandardDocumentUpdate(deleteContent, documentId);

        // Act
        StandardDocumentUpdateResult result = repo.updateDocument(update);

        // Assert
        assertTrue(((DeleteStandardDocumentContentResult) result.updateDocumentContentResult.get()).optimisticLockingError());

        // Verify content still exists
        Optional<StandardDocument> retrievedOptional = repo.getDocumentById(documentId);
        assertTrue(retrievedOptional.isPresent());
        assertTrue(retrievedOptional.get().getContent().isPresent());
        assertEquals(1, retrievedOptional.get().getContent().get().getContent().size());
    }
}