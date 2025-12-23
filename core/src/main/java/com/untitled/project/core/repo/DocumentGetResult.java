package com.untitled.project.core.repo;

import com.untitled.project.core.Document;
import com.untitled.project.core.DocumentContent;
import com.untitled.project.core.identifier.DocumentIdentifier;

public abstract class DocumentGetResult<U, V extends DocumentIdentifier<U>, W extends DocumentContent> {
    abstract Document<U, V, W> getDocument();
}
