package com.untitled.project.models.document.update;


import com.untitled.project.core.repo.DocumentUpdate;
import com.untitled.project.models.document.UuidIdentifier;

public class StandardDocumentUpdate extends DocumentUpdate {
    UpdateType updateType;
    UuidIdentifier documentIdentifier;
    
    public UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    public UuidIdentifier getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(UuidIdentifier documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }
}
