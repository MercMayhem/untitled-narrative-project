package com.untitled.project.models.document;

import com.untitled.project.core.DocumentCollectionInfo;

public class StandardDocumentCollectionInfo implements DocumentCollectionInfo {
    String title;

    public StandardDocumentCollectionInfo(String title) {
        this.title = title;
    }

    @Override
    public String rawString() {
        return title;
    }
}
