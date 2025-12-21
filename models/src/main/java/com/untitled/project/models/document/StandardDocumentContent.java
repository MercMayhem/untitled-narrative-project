package com.untitled.project.models.document;

import java.util.HashMap;

import com.untitled.project.core.DocumentContent;

public class StandardDocumentContent implements DocumentContent {
    HashMap<UuidIdentifier, StandardDocumentContentEntry> content;

    public StandardDocumentContent(HashMap<UuidIdentifier, StandardDocumentContentEntry> content) {
        this.content = content;
    }

    @Override
    public String rawString() {
        // TODO Auto-generated method stub
        return null;
    }

    public HashMap<UuidIdentifier, StandardDocumentContentEntry> getContent() {
        return content;
    }

    public void setContent(HashMap<UuidIdentifier, StandardDocumentContentEntry> content) {
        this.content = content;
    }
}
