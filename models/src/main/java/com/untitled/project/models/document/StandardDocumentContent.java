package com.untitled.project.models.document;

import com.untitled.project.core.DocumentContent;

public class StandardDocumentContent implements DocumentContent {
    String content;

    public StandardDocumentContent(String content) {
        this.content = content;
    }

    @Override
    public String rawString() {
        // TODO Auto-generated method stub
        return null;
    }
}
