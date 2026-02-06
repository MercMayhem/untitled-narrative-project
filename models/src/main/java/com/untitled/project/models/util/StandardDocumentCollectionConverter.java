package com.untitled.project.models.util;

import com.untitled.project.models.document.StandardDocumentCollection;

public interface StandardDocumentCollectionConverter {
    StandardDocumentCollection toStandardDocument();
    void fromStandardDocument(StandardDocumentCollection document);
}
