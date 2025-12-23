package com.untitled.project.core.repo;

import com.untitled.project.core.Document;
import com.untitled.project.core.DocumentContent;
import com.untitled.project.core.identifier.DocumentIdentifier;

public interface DocumentOperations<U, V extends DocumentIdentifier<U>, W extends DocumentContent, X extends DocumentUpdate> {
    public DocumentInsertResult insertDocument(Document<U, V, W> document) throws Exception;
    public DocumentUpdateResult updateDocument(X documentUpdate) throws Exception;
    public DocumentDeleteResult deleteDocument(V id) throws Exception;
    public DocumentInsertResult getDocumentById(V id) throws Exception;
    public DocumentLinkResult linkDocument(V document, V linkDocument) throws Exception;
    public DocumentLinkResult unlinkDocument(V document, V linkDocument) throws Exception;
}
