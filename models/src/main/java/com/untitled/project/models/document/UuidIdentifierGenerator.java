package com.untitled.project.models.document;

import java.util.UUID;

import com.untitled.project.core.identifier.generator.AbstractIdentifierGenerator;

public class UuidIdentifierGenerator extends AbstractIdentifierGenerator<UuidIdentifier, UUID> {

    public UuidIdentifierGenerator() {
        super(UuidIdentifier.class);
    }

    @Override
    public UuidIdentifier generateUnique() {
        UuidIdentifier id = new UuidIdentifier(UUID.randomUUID());
        return id; 
    }
}
