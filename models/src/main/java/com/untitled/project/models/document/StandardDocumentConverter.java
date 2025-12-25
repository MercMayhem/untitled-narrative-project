package com.untitled.project.models.document;

public interface StandardDocumentConverter {
    StandardDocument toStandardDocument();
    void fromStandardDocument(StandardDocument document);
}
