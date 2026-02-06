package com.untitled.project.data;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import com.untitled.project.data.update.DeleteStandardDocumentContentResult;
import com.untitled.project.data.update.InsertStandardDocumentContentResult;
import com.untitled.project.data.update.StandardDocumentUpdateResult;
import com.untitled.project.data.update.UpdateStandardDocumentContentResult;
import com.untitled.project.models.document.*;
import com.untitled.project.models.document.update.DeleteDocumentContent;
import com.untitled.project.models.document.update.InsertDocumentContent;
import com.untitled.project.models.document.update.StandardDocumentUpdate;
import com.untitled.project.models.document.update.UpdateDocumentContent;
import com.untitled.project.models.util.Page;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
    @Test
    void testLinkDocuments() throws SQLException {
        // Arrange - Create two documents
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        // Get current versions
        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        // Act
        repo.linkDocuments(doc1Id, doc2Id);

        // Assert
        Vector<DocumentLinkRecord> links = repo.getDocumentLinks(doc1Id);
        assertEquals(1, links.size());
        assertEquals(doc1Id.value(), links.get(0).getSourceDocumentId());
        assertEquals(doc2Id.value(), links.get(0).getTargetDocumentId());
        assertNotNull(links.get(0).getCreatedAt());

        // Verify incoming links
        Vector<DocumentLinkRecord> incomingLinks = repo.getIncomingDocumentLinks(doc2Id);
        assertEquals(1, incomingLinks.size());
        assertEquals(doc1Id.value(), incomingLinks.get(0).getSourceDocumentId());
        assertEquals(doc2Id.value(), incomingLinks.get(0).getTargetDocumentId());
    }

    @Test
    void testLinkDocumentsWithVersionMismatch() throws SQLException {
        // Arrange - Create two documents
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        // Use wrong version for doc1
        UuidIdentifier doc1IdWrongVersion = new UuidIdentifier(document1.getId().value(), 999L);
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            repo.linkDocuments(doc1IdWrongVersion, doc2Id);
        });
    }

    @Test
    void testLinkDocumentsAlreadyLinked() throws SQLException {
        // Arrange - Create and link two documents
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        repo.linkDocuments(doc1Id, doc2Id);

        // Act - Link again (should update created_at due to ON CONFLICT DO UPDATE)
        repo.linkDocuments(doc1Id, doc2Id);

        // Assert - Still only one link
        Vector<DocumentLinkRecord> links = repo.getDocumentLinks(doc1Id);
        assertEquals(1, links.size());
    }

    @Test
    void testUnlinkDocuments() throws SQLException {
        // Arrange - Create and link two documents
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        repo.linkDocuments(doc1Id, doc2Id);

        // Act
        repo.unlinkDocuments(doc1Id, doc2Id);

        // Assert
        Vector<DocumentLinkRecord> links = repo.getDocumentLinks(doc1Id);
        assertEquals(0, links.size());

        Vector<DocumentLinkRecord> incomingLinks = repo.getIncomingDocumentLinks(doc2Id);
        assertEquals(0, incomingLinks.size());
    }

    @Test
    void testUnlinkDocumentsWithVersionMismatch() throws SQLException {
        // Arrange - Create and link two documents
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        repo.linkDocuments(doc1Id, doc2Id);

        // Try to unlink with wrong version
        UuidIdentifier doc1IdWrongVersion = new UuidIdentifier(doc1Id.value(), 999L);

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            repo.unlinkDocuments(doc1IdWrongVersion, doc2Id);
        });

        // Verify link still exists
        Vector<DocumentLinkRecord> links = repo.getDocumentLinks(doc1Id);
        assertEquals(1, links.size());
    }

    @Test
    void testUnlinkNonExistentLink() throws SQLException {
        // Arrange - Create two documents but don't link them
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1 title", "doc1 content", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2 title", "doc2 content", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);

        repo.insertDocument(document1);
        repo.insertDocument(document2);

        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            repo.unlinkDocuments(doc1Id, doc2Id);
        });
    }

    @Test
    void testGetDocumentLinksMultiple() throws SQLException {
        // Arrange - Create three documents and link doc1 to both doc2 and doc3
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content1 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content2 = new HashMap<>();
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content3 = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        content1.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc1", "content1", new BigDecimal(1)));
        content2.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc2", "content2", new BigDecimal(1)));
        content3.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("doc3", "content3", new BigDecimal(1)));

        StandardDocument document1 = new StandardDocument(content1);
        StandardDocument document2 = new StandardDocument(content2);
        StandardDocument document3 = new StandardDocument(content3);

        repo.insertDocument(document1);
        repo.insertDocument(document2);
        repo.insertDocument(document3);

        StandardDocument currentDoc1 = repo.getDocumentById(document1.getId()).get();
        StandardDocument currentDoc2 = repo.getDocumentById(document2.getId()).get();
        StandardDocument currentDoc3 = repo.getDocumentById(document3.getId()).get();

        UuidIdentifier doc1Id = new UuidIdentifier(currentDoc1.getId().value(), currentDoc1.getId().getVersion());
        UuidIdentifier doc2Id = new UuidIdentifier(currentDoc2.getId().value(), currentDoc2.getId().getVersion());
        UuidIdentifier doc3Id = new UuidIdentifier(currentDoc3.getId().value(), currentDoc3.getId().getVersion());

        // Act
        repo.linkDocuments(doc1Id, doc2Id);
        repo.linkDocuments(doc1Id, doc3Id);

        // Assert
        Vector<DocumentLinkRecord> outgoingLinks = repo.getDocumentLinks(doc1Id);
        assertEquals(2, outgoingLinks.size());

        // Verify both targets are present
        Set<UUID> targetIds = outgoingLinks.stream()
                .map(DocumentLinkRecord::getTargetDocumentId)
                .collect(Collectors.toSet());
        assertTrue(targetIds.contains(doc2Id.value()));
        assertTrue(targetIds.contains(doc3Id.value()));
    }

    // ========== Collection CRUD Tests ==========

    @Test
    void testCreateCollection() throws SQLException {
        // Arrange
        StandardDocumentCollection collection = new StandardDocumentCollection(new StandardDocumentCollectionInfo("My Collection"));
        UuidIdentifier collectionId = collection.getId();

        // Act
        repo.createCollection(collection);

        // Assert
        Optional<StandardDocumentCollection> retrieved = repo.getCollectionById(collectionId);
        assertTrue(retrieved.isPresent());
        assertEquals("My Collection", retrieved.get().getInfo().rawString());
        assertEquals(collectionId.value(), retrieved.get().getId().value());
    }

    @Test
    void testGetAllCollections() throws SQLException {
        // Arrange
        StandardDocumentCollection collection1 = new StandardDocumentCollection(new StandardDocumentCollectionInfo("Collection 1"));
        StandardDocumentCollection collection2 = new StandardDocumentCollection(new StandardDocumentCollectionInfo("Collection 2"));

        repo.createCollection(collection1);
        repo.createCollection(collection2);

        // Act
        Vector<StandardDocumentCollection> collections = repo.getAllCollections();

        // Assert
        assertTrue(collections.size() >= 2);
        Set<String> titles = collections.stream()
                .map(c -> c.getInfo().rawString())
                .collect(Collectors.toSet());
        assertTrue(titles.contains("Collection 1"));
        assertTrue(titles.contains("Collection 2"));
    }

    @Test
    void testUpdateCollection() throws SQLException {
        // Arrange
        StandardDocumentCollection collection = new StandardDocumentCollection(new StandardDocumentCollectionInfo("Original Title"));
        repo.createCollection(collection);

        // Act
        StandardDocumentCollectionInfo updatedInfo = new StandardDocumentCollectionInfo("Updated Title");
        repo.updateCollectionInfo(collection.getId(), updatedInfo);

        // Assert
        Optional<StandardDocumentCollection> retrieved = repo.getCollectionById(collection.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("Updated Title", retrieved.get().getInfo().rawString());
    }

    @Test
    void testUpdateNonExistentCollection() throws SQLException {
        // Arrange
        UuidIdentifier nonExistentId = new UuidIdentifier(UUID.randomUUID(), 0L);
        StandardDocumentCollectionInfo info = new StandardDocumentCollectionInfo("Title");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            repo.updateCollectionInfo(nonExistentId, info);
        });
    }

    @Test
    void testDeleteCollection() throws SQLException {
        // Arrange
        StandardDocumentCollection collection = new StandardDocumentCollection(new StandardDocumentCollectionInfo("To Delete"));
        repo.createCollection(collection);
        UuidIdentifier collectionId = collection.getId();

        // Act
        repo.deleteCollection(collectionId);

        // Assert
        Optional<StandardDocumentCollection> retrieved = repo.getCollectionById(collectionId);
        assertFalse(retrieved.isPresent());
    }

    @Test
    void testDeleteNonExistentCollection() throws SQLException {
        // Arrange
        UuidIdentifier nonExistentId = new UuidIdentifier(UUID.randomUUID(), 0L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            repo.deleteCollection(nonExistentId);
        });
    }

// ========== Pagination Tests for Collections ==========

    @Test
    void testGetAllCollectionsPaginated() throws SQLException {
        // Arrange - Create 15 collections
        for (int i = 1; i <= 15; i++) {
            StandardDocumentCollection collection = new StandardDocumentCollection(
                    new StandardDocumentCollectionInfo("Collection " + i)
            );
            repo.createCollection(collection);
        }

        // Act - Get first page (10 items)
        Page<StandardDocumentCollection> page1 = repo.getAllCollections(1, 10);

        // Assert
        assertEquals(10, page1.getItems().size());
        assertTrue(page1.getTotalCount() >= 15);
        assertEquals(1, page1.getPageNumber());
        assertEquals(10, page1.getPageSize());
        assertTrue(page1.getTotalPages() >= 2);
        assertTrue(page1.hasNext());
        assertFalse(page1.hasPrevious());

        // Act - Get second page
        Page<StandardDocumentCollection> page2 = repo.getAllCollections(2, 10);

        // Assert
        assertTrue(page2.getItems().size() >= 5);
        assertEquals(2, page2.getPageNumber());
    }

    @Test
    void testPaginationEmptyCollections() throws SQLException {
        // Act
        Page<StandardDocumentCollection> page = repo.getAllCollections(1, 10);

        // Assert - May have collections from other tests, but should not fail
        assertTrue(page.getTotalCount() >= 0);
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getPageSize());
    }

    @Test
    void testPaginationSinglePageCollections() throws SQLException {
        // Arrange - Create 5 collections
        for (int i = 1; i <= 5; i++) {
            StandardDocumentCollection collection = new StandardDocumentCollection(
                    new StandardDocumentCollectionInfo("Collection " + i)
            );
            repo.createCollection(collection);
        }

        // Act - Request page size of 10 when only 5 new items exist
        Page<StandardDocumentCollection> page = repo.getAllCollections(1, 10);

        // Assert
        assertTrue(page.getItems().size() >= 5);
        assertEquals(1, page.getPageNumber());
    }

// ========== Document-Collection Association Tests ==========

    @Test
    void testAddDocumentToCollection() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        // Get current document version
        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();
        UuidIdentifier docId = new UuidIdentifier(currentDoc.getId().value(), currentDoc.getId().getVersion());

        // Act
        repo.addDocumentToCollection(docId, collection.getId());

        // Assert
        Vector<DocumentCollectionAssociationRecord> docsInCollection =
                repo.getDocumentsInCollection(collection.getId());
        assertEquals(1, docsInCollection.size());
        assertEquals(docId.value(), docsInCollection.get(0).getDocumentId());
    }

    @Test
    void testAddDocumentToCollectionWithVersionMismatch() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        // Use wrong version
        UuidIdentifier docIdWrongVersion = new UuidIdentifier(document.getId().value(), 999L);

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            repo.addDocumentToCollection(docIdWrongVersion, collection.getId());
        });
    }

    @Test
    void testAddDocumentToCollectionAlreadyInCollection() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();
        repo.addDocumentToCollection(currentDoc.getId(), collection.getId());

        // Act - Add again (should fail due to ON CONFLICT DO NOTHING returning 0 rows)
        assertThrows(SQLException.class, () -> {
            repo.addDocumentToCollection(currentDoc.getId(), collection.getId());
        });
    }

    @Test
    void testRemoveDocumentFromCollection() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();

        repo.addDocumentToCollection(currentDoc.getId(), collection.getId());

        // Act
        repo.removeDocumentFromCollection(currentDoc.getId(), collection.getId());

        // Assert
        Vector<DocumentCollectionAssociationRecord> docsInCollection =
                repo.getDocumentsInCollection(collection.getId());
        assertEquals(0, docsInCollection.size());
    }

    @Test
    void testRemoveDocumentFromCollectionNotInCollection() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();
        UuidIdentifier docId = new UuidIdentifier(currentDoc.getId().value(), currentDoc.getId().getVersion() + 1);

        // Act & Assert
        assertThrows(SQLException.class, () -> {
            repo.removeDocumentFromCollection(docId, collection.getId());
        });
    }

    @Test
    void testGetCollectionsForDocument() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection1 = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("Collection 1")
        );
        StandardDocumentCollection collection2 = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("Collection 2")
        );
        repo.createCollection(collection1);
        repo.createCollection(collection2);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();

        repo.addDocumentToCollection(currentDoc.getId(), collection1.getId());
        repo.addDocumentToCollection(currentDoc.getId(), collection2.getId());

        // Act
        Vector<DocumentCollectionAssociationRecord> collections =
                repo.getCollectionsForDocument(currentDoc.getId());

        // Assert
        assertEquals(2, collections.size());
        Set<UUID> collectionIds = collections.stream()
                .map(DocumentCollectionAssociationRecord::getCollectionId)
                .collect(Collectors.toSet());
        assertTrue(collectionIds.contains(collection1.getId().value()));
        assertTrue(collectionIds.contains(collection2.getId().value()));
    }

    @Test
    void testDeleteCollectionCascadesAssociations() throws SQLException {
        // Arrange
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();

        repo.addDocumentToCollection(currentDoc.getId(), collection.getId());

        // Act
        repo.deleteCollection(collection.getId());

        // Assert - Association should be deleted due to CASCADE
        Vector<DocumentCollectionAssociationRecord> collections =
                repo.getCollectionsForDocument(currentDoc.getId());
        assertEquals(0, collections.size());
    }

    // ========== Pagination Tests for Document-Collection Associations ==========

    @Test
    void testGetDocumentsInCollectionPaginated() throws SQLException {
        // Arrange - Create collection and add 15 documents
        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("My Collection")
        );
        repo.createCollection(collection);

        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        for (int i = 1; i <= 15; i++) {
            HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
            content.put(identifierGenerator.generateUnique(),
                    new StandardDocumentContentEntry("title " + i, "content " + i, new BigDecimal(1)));

            StandardDocument document = new StandardDocument(content);
            repo.insertDocument(document);

            StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();
            repo.addDocumentToCollection(currentDoc.getId(), collection.getId());
        }

        // Act - Get first page
        Page<DocumentCollectionAssociationRecord> page1 =
                repo.getDocumentsInCollection(collection.getId(), 1, 10);

        // Assert
        assertEquals(10, page1.getItems().size());
        assertEquals(15, page1.getTotalCount());
        assertEquals(1, page1.getPageNumber());
        assertEquals(10, page1.getPageSize());
        assertEquals(2, page1.getTotalPages());
        assertTrue(page1.hasNext());
        assertFalse(page1.hasPrevious());

        // Act - Get second page
        Page<DocumentCollectionAssociationRecord> page2 =
                repo.getDocumentsInCollection(collection.getId(), 2, 10);

        // Assert
        assertEquals(5, page2.getItems().size());
        assertEquals(15, page2.getTotalCount());
        assertEquals(2, page2.getPageNumber());
        assertFalse(page2.hasNext());
        assertTrue(page2.hasPrevious());
    }

    @Test
    void testGetCollectionsForDocumentPaginated() throws SQLException {
        // Arrange - Create document and add to 15 collections
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();
        content.put(identifierGenerator.generateUnique(),
                new StandardDocumentContentEntry("title", "content", new BigDecimal(1)));

        StandardDocument document = new StandardDocument(content);
        repo.insertDocument(document);

        StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();

        for (int i = 1; i <= 15; i++) {
            StandardDocumentCollection collection = new StandardDocumentCollection(
                    new StandardDocumentCollectionInfo("Collection " + i)
            );
            repo.createCollection(collection);
            repo.addDocumentToCollection(currentDoc.getId(), collection.getId());
        }

        // Act - Get first page
        Page<DocumentCollectionAssociationRecord> page1 =
                repo.getCollectionsForDocument(currentDoc.getId(), 1, 10);

        // Assert
        assertEquals(10, page1.getItems().size());
        assertEquals(15, page1.getTotalCount());
        assertEquals(1, page1.getPageNumber());
        assertEquals(10, page1.getPageSize());
        assertEquals(2, page1.getTotalPages());
        assertTrue(page1.hasNext());
        assertFalse(page1.hasPrevious());

        // Act - Get second page
        Page<DocumentCollectionAssociationRecord> page2 =
                repo.getCollectionsForDocument(currentDoc.getId(), 2, 10);

        // Assert
        assertEquals(5, page2.getItems().size());
        assertEquals(15, page2.getTotalCount());
        assertEquals(2, page2.getPageNumber());
        assertFalse(page2.hasNext());
        assertTrue(page2.hasPrevious());
    }

    @Test
    void testPaginationEmptyDocumentsInCollection() throws SQLException {
        // Arrange
        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("Empty Collection")
        );
        repo.createCollection(collection);

        // Act
        Page<DocumentCollectionAssociationRecord> page =
                repo.getDocumentsInCollection(collection.getId(), 1, 10);

        // Assert
        assertEquals(0, page.getItems().size());
        assertEquals(0, page.getTotalCount());
        assertEquals(0, page.getTotalPages());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
    }

    @Test
    void testPaginationSinglePageDocumentsInCollection() throws SQLException {
        // Arrange - Create collection with 5 documents
        StandardDocumentCollection collection = new StandardDocumentCollection(
                new StandardDocumentCollectionInfo("Small Collection")
        );
        repo.createCollection(collection);

        UuidIdentifierGenerator identifierGenerator = new UuidIdentifierGenerator();

        for (int i = 1; i <= 5; i++) {
            HashMap<UuidIdentifier, StandardDocumentContentEntry> content = new HashMap<>();
            content.put(identifierGenerator.generateUnique(),
                    new StandardDocumentContentEntry("title " + i, "content " + i, new BigDecimal(1)));

            StandardDocument document = new StandardDocument(content);
            repo.insertDocument(document);

            StandardDocument currentDoc = repo.getDocumentById(document.getId()).get();

            repo.addDocumentToCollection(currentDoc.getId(), collection.getId());
        }

        // Act - Request page size of 10 when only 5 items exist
        Page<DocumentCollectionAssociationRecord> page =
                repo.getDocumentsInCollection(collection.getId(), 1, 10);

        // Assert
        assertEquals(5, page.getItems().size());
        assertEquals(5, page.getTotalCount());
        assertEquals(1, page.getPageNumber());
        assertEquals(1, page.getTotalPages());
        assertFalse(page.hasNext());
        assertFalse(page.hasPrevious());
    }
}