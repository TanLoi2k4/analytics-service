package com.tlcn.analytics_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_performance")
@Getter
@Setter
public class ProductPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long productId;

    private String productName;

    private Long totalQuantitySold = 0L;

    private BigDecimal totalRevenue = BigDecimal.ZERO;

    private String vendorId;

    public ProductPerformance() {
        super();
        this.totalQuantitySold = 0L;
        this.totalRevenue = BigDecimal.ZERO;
    }

    public ProductPerformance(
        Long id,
        Long productId,
        String productName,
        Long totalQuantitySold,
        BigDecimal totalRevenue,
        String vendorId
    ) {
        super();
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.totalQuantitySold = totalQuantitySold;
        this.totalRevenue = totalRevenue;
        this.vendorId = vendorId;
    }

    public static ProductPerformanceBuilder builder() {
        return new ProductPerformanceBuilder();
    }

    public static class ProductPerformanceBuilder {
        private Long id;
        private Long productId;
        private String productName;
        private Long totalQuantitySold;
        private boolean totalQuantitySold$set;
        private BigDecimal totalRevenue;
        private boolean totalRevenue$set;
        private String vendorId;

        public ProductPerformanceBuilder() {
            super();
        }

        public ProductPerformanceBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProductPerformanceBuilder productId(Long productId) {
            this.productId = productId;
            return this;
        }

        public ProductPerformanceBuilder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public ProductPerformanceBuilder totalQuantitySold(Long totalQuantitySold) {
            this.totalQuantitySold = totalQuantitySold;
            this.totalQuantitySold$set = true;
            return this;
        }

        public ProductPerformanceBuilder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            this.totalRevenue$set = true;
            return this;
        }

        public ProductPerformanceBuilder vendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public ProductPerformance build() {
            return new ProductPerformance(
                this.id,
                this.productId,
                this.productName,
                this.totalQuantitySold$set ? this.totalQuantitySold : 0L,
                this.totalRevenue$set ? this.totalRevenue : BigDecimal.ZERO,
                this.vendorId
            );
        }
    }
}
