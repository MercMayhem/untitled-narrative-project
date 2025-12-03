package com.untitled.project.core.identifier;

public interface IdentifierDeserializer<T> {
    Identifier<T> fromRawString(String rawString);
}
