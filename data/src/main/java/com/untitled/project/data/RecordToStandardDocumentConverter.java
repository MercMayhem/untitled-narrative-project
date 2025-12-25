package com.untitled.project.data;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.untitled.project.core.DocumentContent;
import com.untitled.project.models.document.StandardDocument;
import com.untitled.project.models.document.StandardDocumentContentEntry;
import com.untitled.project.models.document.StandardDocumentConverter;
import com.untitled.project.models.document.UuidIdentifier;

public class RecordToStandardDocumentConverter implements StandardDocumentConverter {
    StandardDocumentRecord documentRecord;
    Vector<StandardDocumentContentRecord> documentContentRecords;
    
    public RecordToStandardDocumentConverter(StandardDocumentRecord documentRecord, Vector<StandardDocumentContentRecord> documentContentRecords) {
        this.documentRecord = documentRecord;
        this.documentContentRecords = documentContentRecords;
    }

    @Override
    public void fromStandardDocument(StandardDocument document) {
        // TODO Auto-generated method stub
        
    }
    @Override
    public StandardDocument toStandardDocument() {
        UUID id = this.documentRecord.id;
        Long version = this.documentRecord.version;
        HashMap<UuidIdentifier, StandardDocumentContentEntry> content =
            this.documentContentRecords
                .stream()
                .map(new Function<StandardDocumentContentRecord, SimpleEntry<UuidIdentifier, StandardDocumentContentEntry>>() {
                    public SimpleEntry<UuidIdentifier, StandardDocumentContentEntry> apply(StandardDocumentContentRecord contentRecord) {
                        UuidIdentifier contentId = new UuidIdentifier(contentRecord.getId(), contentRecord.getVersion());
                        StandardDocumentContentEntry contentEntry = new StandardDocumentContentEntry(contentRecord.getTitle(), contentRecord.getContent());
                        return new SimpleEntry<UuidIdentifier, StandardDocumentContentEntry>(contentId, contentEntry);
                    }
                })
                .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> b, HashMap::new));

        StandardDocument standardDocument = new StandardDocument(id, content, version);
        return standardDocument;
    }
}
