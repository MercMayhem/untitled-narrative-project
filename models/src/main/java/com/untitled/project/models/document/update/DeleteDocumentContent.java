package com.untitled.project.models.document.update;

import com.untitled.project.models.document.UuidIdentifier;

public record DeleteDocumentContent(UuidIdentifier id) implements UpdateType {
    
}
