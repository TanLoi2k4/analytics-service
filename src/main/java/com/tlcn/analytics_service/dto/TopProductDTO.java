package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopProductDTO {
    private Long productId;
    private String name;
    private Long quantity;
    private Long sales;
    private BigDecimal revenue;
    private Integer stock;
    private Double rating;
    private String imageUrl;
    private String categoryName;

    public TopProductDTO() {
        super();
    }

    public TopProductDTO(
        Long productId,
        String name,
        Long quantity,
        Long sales,
        BigDecimal revenue,
        Integer stock,
        Double rating,
        String imageUrl,
        String categoryName
    ) {
        super();
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.sales = sales;
        this.revenue = revenue;
        this.stock = stock;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.categoryName = categoryName;
    }

    public static TopProductDTOBuilder builder() {
        return new TopProductDTOBuilder();
    }

    public static class TopProductDTOBuilder {
        private Long productId;
        private String name;
        private Long quantity;
        private Long sales;
        private BigDecimal revenue;
        private Integer stock;
        private Double rating;
        private String imageUrl;
        private String categoryName;

        public TopProductDTOBuilder() {
            super();
        }

        public TopProductDTOBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public TopProductDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TopProductDTOBuilder quantity(Long quantity) {
            this.quantity = quantity;
            return this;
        }

        public TopProductDTOBuilder sales(Long sales) {
            this.sales = sales;
            return this;
        }

        public TopProductDTOBuilder revenue(BigDecimal revenue) {
            this.revenue = revenue;
            return this;
        }

        public TopProductDTOBuilder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public TopProductDTOBuilder rating(Double rating) {
            this.rating = rating;
            return this;
        }

        public TopProductDTOBuilder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public TopProductDTOBuilder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public TopProductDTO build() {
            return new TopProductDTO(
                this.productId,
                this.name,
                this.quantity,
                this.sales,
                this.revenue,
                this.stock,
                this.rating,
                this.imageUrl,
                this.categoryName
            );
        }
    }
}
