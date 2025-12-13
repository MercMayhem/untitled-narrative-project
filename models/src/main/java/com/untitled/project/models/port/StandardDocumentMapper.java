package com.untitled.project.models.port;

import com.untitled.project.models.document.StandardDocument;

public interface StandardDocumentMapper<T> {
    public StandardDocument toStandardDocument();
    public T fromStandardDocument();
}
