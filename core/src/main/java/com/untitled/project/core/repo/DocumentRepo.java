package com.untitled.project.core.repo;

import com.untitled.project.core.Document;
import com.untitled.project.core.identifier.DocumentIdentifier;

public interface DocumentRepo<U, V extends DocumentIdentifier<U>> {
    public void insertDocument(Document<U, V> document);
    public Document<U, V> getDocumentCollectionById(V id);
}
