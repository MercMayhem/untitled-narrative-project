package com.untitled.project.core.repo;

import com.untitled.project.core.DocumentCollection;
import com.untitled.project.core.DocumentCollectionInfo;
import com.untitled.project.core.identifier.DocumentCollectionIdentifier;

public interface DocumentCollectionOperations<U, V extends DocumentCollectionIdentifier<U>, W extends DocumentCollectionInfo> {
    public void insertDocumentCollection(DocumentCollection<U, V, W> documentCollection);
    public DocumentCollection<U, V, W> getDocumentCollectionById(DocumentCollectionIdentifier<U> id);
}
