package com.untitled.project.models.document.update;

public sealed interface UpdateType
        permits DeleteDocumentContent,
                InsertDocumentContent,
                UpdateDocumentContent {
    
}
