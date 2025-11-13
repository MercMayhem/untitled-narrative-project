package com.untitled.project.core.identifier;

import java.util.Objects;

public abstract class AbstractIdentifier<T> implements Identifier<T> {
    private final T value;

    protected AbstractIdentifier(T value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public final T value() {
        return value;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractIdentifier<?> other = (AbstractIdentifier<?>) obj;
        return value.equals(other.value);
    }

    @Override
    public final int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + value + ")";
    }
}
