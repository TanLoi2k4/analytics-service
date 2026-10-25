package com.tlcn.analytics_service.dto.external;

import lombok.Data;

import java.util.List;

@Data
public class ProductSnapshotDto {
    private Long id;
    private String name;
    private Integer stock;
    private Double rating;
    private String imageUrl;
    private List<ImageSnapshotDto> images;
    private CategorySnapshotDto category;

    public ProductSnapshotDto() {
        super();
    }
    @Data
    public static class CategorySnapshotDto {
        private String name;

        public CategorySnapshotDto() {
            super();
        }
    }

    @Data
    public static class ImageSnapshotDto {
        private String imageUrl;
        private boolean main;

        public ImageSnapshotDto() {
            super();
        }
    }

    public String resolveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl.trim();
        }
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
            .filter(img -> img != null && img.isMain())
            .map(ImageSnapshotDto::getImageUrl)
            .filter(url -> url != null && !url.isBlank())
            .findFirst()
            .or(() -> images.stream()
                .map(img -> img == null ? null : img.getImageUrl())
                .filter(url -> url != null && !url.isBlank())
                .findFirst())
            .map(String::trim)
            .orElse(null);
    }
}
