package com.untitled.project.models.util;

import java.util.Vector;

public class Page<T> {
    private final Vector<T> items;
    private final long totalCount;
    private final int pageNumber;
    private final int pageSize;
    private final int totalPages;

    public Page(Vector<T> items, long totalCount, int pageNumber, int pageSize) {
        this.items = items;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    public Vector<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return pageNumber < totalPages;
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }
}