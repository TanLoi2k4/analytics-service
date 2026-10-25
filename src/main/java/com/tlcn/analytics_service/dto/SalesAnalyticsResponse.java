package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SalesAnalyticsResponse {
    private SummaryStatsDTO summary;

    private List<ChartPointDTO> chartData = new ArrayList<>();

    private List<ChartPointDTO> weeklyData = new ArrayList<>();

    public SalesAnalyticsResponse() {
        super();
        this.chartData = new ArrayList<>();
        this.weeklyData = new ArrayList<>();
    }

    public SalesAnalyticsResponse(
        SummaryStatsDTO summary,
        List<ChartPointDTO> chartData,
        List<ChartPointDTO> weeklyData
    ) {
        super();
        this.summary = summary;
        this.chartData = chartData;
        this.weeklyData = weeklyData;
    }

    public static SalesAnalyticsResponseBuilder builder() {
        return new SalesAnalyticsResponseBuilder();
    }

    public static class SalesAnalyticsResponseBuilder {
        private SummaryStatsDTO summary;
        private List<ChartPointDTO> chartData;
        private boolean chartData$set;
        private List<ChartPointDTO> weeklyData;
        private boolean weeklyData$set;

        public SalesAnalyticsResponseBuilder() {
            super();
        }

        public SalesAnalyticsResponseBuilder summary(SummaryStatsDTO summary) {
            this.summary = summary;
            return this;
        }

        public SalesAnalyticsResponseBuilder chartData(List<ChartPointDTO> chartData) {
            this.chartData = chartData;
            this.chartData$set = true;
            return this;
        }

        public SalesAnalyticsResponseBuilder weeklyData(List<ChartPointDTO> weeklyData) {
            this.weeklyData = weeklyData;
            this.weeklyData$set = true;
            return this;
        }

        public SalesAnalyticsResponse build() {
            return new SalesAnalyticsResponse(
                this.summary,
                this.chartData$set ? this.chartData : new ArrayList<ChartPointDTO>(),
                this.weeklyData$set ? this.weeklyData : new ArrayList<ChartPointDTO>()
            );
        }
    }
}
