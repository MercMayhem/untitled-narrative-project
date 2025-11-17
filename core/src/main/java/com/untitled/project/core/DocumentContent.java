package com.untitled.project.core;

public interface DocumentContent {
    String rawString();
 
    default boolean isPartial() {
        return false;
    }
}
