package com.untitled.project.core.repo;

import java.sql.SQLException;

import com.untitled.project.core.Document;
import com.untitled.project.core.DocumentContent;
import com.untitled.project.core.identifier.DocumentIdentifier;

public interface DocumentRepo<U, V extends DocumentIdentifier<U>, W extends DocumentContent> {
    public void upsertDocument(Document<U, V, W> document) throws SQLException;
    public void deleteDocument(V id) throws SQLException;
    public Document<U, V, W> getDocumentById(V id) throws SQLException;
    public void linkDocument(V document, V linkDocument) throws SQLException;
    public void unlinkDocument(V document, V linkDocument) throws SQLException;
}
