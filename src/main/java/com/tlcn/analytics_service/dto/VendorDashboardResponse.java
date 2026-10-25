package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorDashboardResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double revenueGrowth;
    private Double ordersGrowth;
    private TopProductDTO topProduct;
    private BigDecimal avgOrderValue;

    public VendorDashboardResponse() {
        super();
    }

    public VendorDashboardResponse(
        BigDecimal totalRevenue,
        Long totalOrders,
        Double revenueGrowth,
        Double ordersGrowth,
        TopProductDTO topProduct,
        BigDecimal avgOrderValue
    ) {
        super();
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.revenueGrowth = revenueGrowth;
        this.ordersGrowth = ordersGrowth;
        this.topProduct = topProduct;
        this.avgOrderValue = avgOrderValue;
    }

    public static VendorDashboardResponseBuilder builder() {
        return new VendorDashboardResponseBuilder();
    }

    public static class VendorDashboardResponseBuilder {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Double revenueGrowth;
        private Double ordersGrowth;
        private TopProductDTO topProduct;
        private BigDecimal avgOrderValue;

        public VendorDashboardResponseBuilder() {
            super();
        }

        public VendorDashboardResponseBuilder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }

        public VendorDashboardResponseBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public VendorDashboardResponseBuilder revenueGrowth(Double revenueGrowth) {
            this.revenueGrowth = revenueGrowth;
            return this;
        }

        public VendorDashboardResponseBuilder ordersGrowth(Double ordersGrowth) {
            this.ordersGrowth = ordersGrowth;
            return this;
        }

        public VendorDashboardResponseBuilder topProduct(TopProductDTO topProduct) {
            this.topProduct = topProduct;
            return this;
        }

        public VendorDashboardResponseBuilder avgOrderValue(BigDecimal avgOrderValue) {
            this.avgOrderValue = avgOrderValue;
            return this;
        }

        public VendorDashboardResponse build() {
            return new VendorDashboardResponse(
                this.totalRevenue,
                this.totalOrders,
                this.revenueGrowth,
                this.ordersGrowth,
                this.topProduct,
                this.avgOrderValue
            );
        }
    }
}
