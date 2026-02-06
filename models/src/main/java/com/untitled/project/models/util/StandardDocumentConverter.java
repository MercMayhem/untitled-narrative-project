package com.untitled.project.models.util;

import com.untitled.project.models.document.StandardDocument;

public interface StandardDocumentConverter {
    StandardDocument toStandardDocument();
    void fromStandardDocument(StandardDocument document);
}
