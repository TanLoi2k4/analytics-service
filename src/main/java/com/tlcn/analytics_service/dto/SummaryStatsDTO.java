package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SummaryStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double growth;
    private Double ordersGrowth;
    private BigDecimal avgOrderValue;
    private Double avgOrderValueGrowth;
    private Long totalCustomers;
    private Long newCustomers;
    private BigDecimal commission;
    private BigDecimal netRevenue;

    public SummaryStatsDTO() {
        super();
    }

    public SummaryStatsDTO(
        BigDecimal totalRevenue,
        Long totalOrders,
        Double growth,
        Double ordersGrowth,
        BigDecimal avgOrderValue,
        Double avgOrderValueGrowth,
        Long totalCustomers,
        Long newCustomers,
        BigDecimal commission,
        BigDecimal netRevenue
    ) {
        super();
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.growth = growth;
        this.ordersGrowth = ordersGrowth;
        this.avgOrderValue = avgOrderValue;
        this.avgOrderValueGrowth = avgOrderValueGrowth;
        this.totalCustomers = totalCustomers;
        this.newCustomers = newCustomers;
        this.commission = commission;
        this.netRevenue = netRevenue;
    }

    public static SummaryStatsDTOBuilder builder() {
        return new SummaryStatsDTOBuilder();
    }

    public static class SummaryStatsDTOBuilder {
        private BigDecimal totalRevenue;
        private Long totalOrders;
        private Double growth;
        private Double ordersGrowth;
        private BigDecimal avgOrderValue;
        private Double avgOrderValueGrowth;
        private Long totalCustomers;
        private Long newCustomers;
        private BigDecimal commission;
        private BigDecimal netRevenue;

        public SummaryStatsDTOBuilder() {
            super();
        }

        public SummaryStatsDTOBuilder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }

        public SummaryStatsDTOBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public SummaryStatsDTOBuilder growth(Double growth) {
            this.growth = growth;
            return this;
        }

        public SummaryStatsDTOBuilder ordersGrowth(Double ordersGrowth) {
            this.ordersGrowth = ordersGrowth;
            return this;
        }

        public SummaryStatsDTOBuilder avgOrderValue(BigDecimal avgOrderValue) {
            this.avgOrderValue = avgOrderValue;
            return this;
        }

        public SummaryStatsDTOBuilder avgOrderValueGrowth(Double avgOrderValueGrowth) {
            this.avgOrderValueGrowth = avgOrderValueGrowth;
            return this;
        }

        public SummaryStatsDTOBuilder totalCustomers(Long totalCustomers) {
            this.totalCustomers = totalCustomers;
            return this;
        }

        public SummaryStatsDTOBuilder newCustomers(Long newCustomers) {
            this.newCustomers = newCustomers;
            return this;
        }

        public SummaryStatsDTOBuilder commission(BigDecimal commission) {
            this.commission = commission;
            return this;
        }

        public SummaryStatsDTOBuilder netRevenue(BigDecimal netRevenue) {
            this.netRevenue = netRevenue;
            return this;
        }

        public SummaryStatsDTO build() {
            return new SummaryStatsDTO(
                this.totalRevenue,
                this.totalOrders,
                this.growth,
                this.ordersGrowth,
                this.avgOrderValue,
                this.avgOrderValueGrowth,
                this.totalCustomers,
                this.newCustomers,
                this.commission,
                this.netRevenue
            );
        }
    }
}
