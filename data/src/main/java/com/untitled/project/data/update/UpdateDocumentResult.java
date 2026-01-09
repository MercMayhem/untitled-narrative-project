package com.untitled.project.data.update;

public sealed interface UpdateDocumentResult permits 
    InsertStandardDocumentContentResult,
    DeleteStandardDocumentContentResult,
    UpdateStandardDocumentContentResult {
    
}
