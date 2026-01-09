package com.untitled.project.data.update;

import java.util.Vector;

import com.untitled.project.data.StandardDocumentContentRecord;

public record InsertStandardDocumentContentResult(boolean optimisticLockingError, Vector<StandardDocumentContentRecord> createdDocumentContent) implements UpdateDocumentResult {
    
}
