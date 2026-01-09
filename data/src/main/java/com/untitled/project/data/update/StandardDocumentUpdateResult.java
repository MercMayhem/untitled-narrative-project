package com.untitled.project.data.update;

import java.util.Optional;

public class StandardDocumentUpdateResult {
    public Optional<UpdateDocumentResult> updateDocumentContentResult;
    
    public StandardDocumentUpdateResult(UpdateDocumentResult updateDocumentContentResult) {
        this.updateDocumentContentResult = Optional.ofNullable(updateDocumentContentResult);
    }

    public Optional<UpdateDocumentResult> getUpdateDocumentContentResult() {
        return updateDocumentContentResult;
    }
}
