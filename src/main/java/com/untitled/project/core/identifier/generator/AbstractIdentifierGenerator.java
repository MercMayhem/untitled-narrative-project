package com.untitled.project.core.identifier.generator;

import com.untitled.project.core.identifier.Identifier;

public abstract class AbstractIdentifierGenerator<T extends Identifier<U>, U> implements IdentifierGenerator<T, U>{
    protected final Class<T> identifierClass;

    public AbstractIdentifierGenerator(Class<T> identifierType) {
        this.identifierClass = identifierType;
    }

    public abstract T generateUnique();
}
