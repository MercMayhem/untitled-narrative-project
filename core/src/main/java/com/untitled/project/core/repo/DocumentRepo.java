package com.untitled.project.core.repo;

import java.sql.SQLException;

import com.untitled.project.core.Document;
import com.untitled.project.core.identifier.DocumentIdentifier;

public interface DocumentRepo<U, V extends DocumentIdentifier<U>> {
    public void upsertDocument(Document<U, V> document) throws SQLException;
    public void deleteDocument(V id);
    public Document<U, V> getDocumentCollectionById(V id);
    public void linkDocument(V document, V linkDocument);
    public void unlinkDocument(V document, V linkDocument);
}
