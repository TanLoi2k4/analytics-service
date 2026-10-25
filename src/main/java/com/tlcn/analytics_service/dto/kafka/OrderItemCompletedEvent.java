package com.tlcn.analytics_service.dto.kafka;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCompletedEvent {
    private Long productId;
    private String productName;
    private String categoryName;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public OrderItemCompletedEvent() {
        super();
    }

    public OrderItemCompletedEvent(
        Long productId,
        String productName,
        String categoryName,
        Long quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
    ) {
        super();
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public static OrderItemCompletedEventBuilder builder() {
        return new OrderItemCompletedEventBuilder();
    }

    public static class OrderItemCompletedEventBuilder {
        private Long productId;
        private String productName;
        private String categoryName;
        private Long quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public OrderItemCompletedEventBuilder() {
            super();
        }

        public OrderItemCompletedEventBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public OrderItemCompletedEventBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public OrderItemCompletedEventBuilder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public OrderItemCompletedEventBuilder quantity(Long quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItemCompletedEventBuilder unitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public OrderItemCompletedEventBuilder lineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
            return this;
        }

        public OrderItemCompletedEvent build() {
            return new OrderItemCompletedEvent(
                this.productId,
                this.productName,
                this.categoryName,
                this.quantity,
                this.unitPrice,
                this.lineTotal
            );
        }
    }
}
