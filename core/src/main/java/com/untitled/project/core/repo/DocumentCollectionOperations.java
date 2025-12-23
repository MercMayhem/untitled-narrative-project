package com.untitled.project.core.repo;

import com.untitled.project.core.DocumentCollection;
import com.untitled.project.core.identifier.DocumentCollectionIdentifier;

public interface DocumentCollectionOperations<U, V extends DocumentCollectionIdentifier<U>> {
    public void insertDocumentCollection(DocumentCollection<U, V> documentCollection);
    public DocumentCollection<U, V> getDocumentCollectionById(DocumentCollectionIdentifier<U> id);
}
