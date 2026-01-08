package com.untitled.project.models.document.update;

import com.untitled.project.models.document.StandardDocumentContent;

public record InsertDocumentContent(StandardDocumentContent insertStandardDocumentContent) implements UpdateType {

    public StandardDocumentContent insertStandardDocumentContent() {
        return insertStandardDocumentContent;
    }
    
}
