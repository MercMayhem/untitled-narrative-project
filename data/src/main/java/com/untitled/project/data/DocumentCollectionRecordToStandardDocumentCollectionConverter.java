package com.untitled.project.data;

import com.untitled.project.models.document.StandardDocumentCollection;
import com.untitled.project.models.document.StandardDocumentCollectionInfo;
import com.untitled.project.models.document.UuidIdentifier;
import com.untitled.project.models.util.StandardDocumentCollectionConverter;

public class DocumentCollectionRecordToStandardDocumentCollectionConverter implements StandardDocumentCollectionConverter {
    DocumentCollectionRecord documentCollectionRecord;

    public DocumentCollectionRecordToStandardDocumentCollectionConverter(DocumentCollectionRecord documentCollectionRecord) {
        this.documentCollectionRecord = documentCollectionRecord;
    }

    @Override
    public StandardDocumentCollection toStandardDocument() {
        UuidIdentifier identifier = new UuidIdentifier(documentCollectionRecord.getId());
        StandardDocumentCollectionInfo documentCollectionInfo = new StandardDocumentCollectionInfo(documentCollectionRecord.getTitle());
        return new StandardDocumentCollection(identifier, documentCollectionInfo);
    }

    @Override
    public void fromStandardDocument(StandardDocumentCollection document) {

    }
}
