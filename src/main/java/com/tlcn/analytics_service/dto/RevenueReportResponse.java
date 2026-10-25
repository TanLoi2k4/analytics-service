package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RevenueReportResponse {
    private SummaryStatsDTO summary;

    private List<ChartPointDTO> chartData = new ArrayList<>();

    private List<CategoryBreakdownDTO> categoryData = new ArrayList<>();

    public RevenueReportResponse() {
        super();
        this.chartData = new ArrayList<>();
        this.categoryData = new ArrayList<>();
    }

    public RevenueReportResponse(
        SummaryStatsDTO summary,
        List<ChartPointDTO> chartData,
        List<CategoryBreakdownDTO> categoryData
    ) {
        super();
        this.summary = summary;
        this.chartData = chartData;
        this.categoryData = categoryData;
    }

    public static RevenueReportResponseBuilder builder() {
        return new RevenueReportResponseBuilder();
    }

    public static class RevenueReportResponseBuilder {
        private SummaryStatsDTO summary;
        private List<ChartPointDTO> chartData;
        private boolean chartData$set;
        private List<CategoryBreakdownDTO> categoryData;
        private boolean categoryData$set;

        public RevenueReportResponseBuilder() {
            super();
        }

        public RevenueReportResponseBuilder summary(SummaryStatsDTO summary) {
            this.summary = summary;
            return this;
        }

        public RevenueReportResponseBuilder chartData(List<ChartPointDTO> chartData) {
            this.chartData = chartData;
            this.chartData$set = true;
            return this;
        }

        public RevenueReportResponseBuilder categoryData(List<CategoryBreakdownDTO> categoryData) {
            this.categoryData = categoryData;
            this.categoryData$set = true;
            return this;
        }

        public RevenueReportResponse build() {
            return new RevenueReportResponse(
                this.summary,
                this.chartData$set ? this.chartData : new ArrayList<ChartPointDTO>(),
                this.categoryData$set ? this.categoryData : new ArrayList<CategoryBreakdownDTO>()
            );
        }
    }
}
