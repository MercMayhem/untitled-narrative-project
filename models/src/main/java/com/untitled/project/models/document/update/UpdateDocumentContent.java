package com.untitled.project.models.document.update;

import com.untitled.project.models.document.StandardDocumentContent;

public record UpdateDocumentContent(StandardDocumentContent updateDocumentContent) implements UpdateType {
    
}
