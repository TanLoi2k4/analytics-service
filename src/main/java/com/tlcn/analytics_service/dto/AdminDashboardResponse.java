package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminDashboardResponse {
    private BigDecimal gmv;
    private Long totalOrders;
    private Long totalUsers;
    private Long totalVendors;
    private Double gmvGrowth;
    private Double ordersGrowth;
    private Double userGrowth;
    private BigDecimal commissionEarned;

    public AdminDashboardResponse() {
        super();
    }

    public AdminDashboardResponse(
        BigDecimal gmv,
        Long totalOrders,
        Long totalUsers,
        Long totalVendors,
        Double gmvGrowth,
        Double ordersGrowth,
        Double userGrowth,
        BigDecimal commissionEarned
    ) {
        super();
        this.gmv = gmv;
        this.totalOrders = totalOrders;
        this.totalUsers = totalUsers;
        this.totalVendors = totalVendors;
        this.gmvGrowth = gmvGrowth;
        this.ordersGrowth = ordersGrowth;
        this.userGrowth = userGrowth;
        this.commissionEarned = commissionEarned;
    }

    public static AdminDashboardResponseBuilder builder() {
        return new AdminDashboardResponseBuilder();
    }

    public static class AdminDashboardResponseBuilder {
        private BigDecimal gmv;
        private Long totalOrders;
        private Long totalUsers;
        private Long totalVendors;
        private Double gmvGrowth;
        private Double ordersGrowth;
        private Double userGrowth;
        private BigDecimal commissionEarned;

        public AdminDashboardResponseBuilder() {
            super();
        }

        public AdminDashboardResponseBuilder gmv(BigDecimal gmv) {
            this.gmv = gmv;
            return this;
        }

        public AdminDashboardResponseBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public AdminDashboardResponseBuilder totalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
            return this;
        }

        public AdminDashboardResponseBuilder totalVendors(Long totalVendors) {
            this.totalVendors = totalVendors;
            return this;
        }

        public AdminDashboardResponseBuilder gmvGrowth(Double gmvGrowth) {
            this.gmvGrowth = gmvGrowth;
            return this;
        }

        public AdminDashboardResponseBuilder ordersGrowth(Double ordersGrowth) {
            this.ordersGrowth = ordersGrowth;
            return this;
        }

        public AdminDashboardResponseBuilder userGrowth(Double userGrowth) {
            this.userGrowth = userGrowth;
            return this;
        }

        public AdminDashboardResponseBuilder commissionEarned(BigDecimal commissionEarned) {
            this.commissionEarned = commissionEarned;
            return this;
        }

        public AdminDashboardResponse build() {
            return new AdminDashboardResponse(
                this.gmv,
                this.totalOrders,
                this.totalUsers,
                this.totalVendors,
                this.gmvGrowth,
                this.ordersGrowth,
                this.userGrowth,
                this.commissionEarned
            );
        }
    }
}
