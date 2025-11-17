package com.untitled.project.core.identifier.generator;

import com.untitled.project.core.identifier.Identifier;

public interface IdentifierGenerator<T extends Identifier<U>, U> {
    public T generateUnique();
}
