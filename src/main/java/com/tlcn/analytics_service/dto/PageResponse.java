package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PageResponse() {
        super();
    }

    public PageResponse(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
    ) {
        super();
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    public static <T> PageResponseBuilder<T> builder() {
        return new PageResponseBuilder<>();
    }

    public static class PageResponseBuilder<T> {
        private List<T> items;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean last;

        public PageResponseBuilder() {
            super();
        }

        public PageResponseBuilder<T> items(List<T> items) {
            this.items = items;
            return this;
        }

        public PageResponseBuilder<T> page(int page) {
            this.page = page;
            return this;
        }

        public PageResponseBuilder<T> size(int size) {
            this.size = size;
            return this;
        }

        public PageResponseBuilder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public PageResponseBuilder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageResponseBuilder<T> last(boolean last) {
            this.last = last;
            return this;
        }

        public PageResponse<T> build() {
            return new PageResponse<>(
                this.items,
                this.page,
                this.size,
                this.totalElements,
                this.totalPages,
                this.last
            );
        }
    }
}
