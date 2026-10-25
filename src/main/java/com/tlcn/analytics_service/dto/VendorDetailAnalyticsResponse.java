package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class VendorDetailAnalyticsResponse {
    private String vendorId;
    private SummaryStatsDTO summary;

    private List<ChartPointDTO> revenue = new ArrayList<>();

    private List<ChartPointDTO> orders = new ArrayList<>();

    private List<TopProductDTO> topProducts = new ArrayList<>();

    public VendorDetailAnalyticsResponse() {
        super();
        this.revenue = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.topProducts = new ArrayList<>();
    }

    public VendorDetailAnalyticsResponse(
        String vendorId,
        SummaryStatsDTO summary,
        List<ChartPointDTO> revenue,
        List<ChartPointDTO> orders,
        List<TopProductDTO> topProducts
    ) {
        super();
        this.vendorId = vendorId;
        this.summary = summary;
        this.revenue = revenue;
        this.orders = orders;
        this.topProducts = topProducts;
    }

    public static VendorDetailAnalyticsResponseBuilder builder() {
        return new VendorDetailAnalyticsResponseBuilder();
    }

    public static class VendorDetailAnalyticsResponseBuilder {
        private String vendorId;
        private SummaryStatsDTO summary;
        private List<ChartPointDTO> revenue;
        private boolean revenue$set;
        private List<ChartPointDTO> orders;
        private boolean orders$set;
        private List<TopProductDTO> topProducts;
        private boolean topProducts$set;

        public VendorDetailAnalyticsResponseBuilder() {
            super();
        }

        public VendorDetailAnalyticsResponseBuilder vendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public VendorDetailAnalyticsResponseBuilder summary(SummaryStatsDTO summary) {
            this.summary = summary;
            return this;
        }

        public VendorDetailAnalyticsResponseBuilder revenue(List<ChartPointDTO> revenue) {
            this.revenue = revenue;
            this.revenue$set = true;
            return this;
        }

        public VendorDetailAnalyticsResponseBuilder orders(List<ChartPointDTO> orders) {
            this.orders = orders;
            this.orders$set = true;
            return this;
        }

        public VendorDetailAnalyticsResponseBuilder topProducts(List<TopProductDTO> topProducts) {
            this.topProducts = topProducts;
            this.topProducts$set = true;
            return this;
        }

        public VendorDetailAnalyticsResponse build() {
            return new VendorDetailAnalyticsResponse(
                this.vendorId,
                this.summary,
                this.revenue$set ? this.revenue : new ArrayList<ChartPointDTO>(),
                this.orders$set ? this.orders : new ArrayList<ChartPointDTO>(),
                this.topProducts$set ? this.topProducts : new ArrayList<TopProductDTO>()
            );
        }
    }
}
