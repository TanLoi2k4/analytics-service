package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CustomerGrowthResponse {
    private List<ChartPointDTO> chartData = new ArrayList<>();

    public CustomerGrowthResponse() {
        super();
        this.chartData = new ArrayList<>();
    }

    public CustomerGrowthResponse(List<ChartPointDTO> chartData) {
        super();
        this.chartData = chartData;
    }

    public static CustomerGrowthResponseBuilder builder() {
        return new CustomerGrowthResponseBuilder();
    }

    public static class CustomerGrowthResponseBuilder {
        private List<ChartPointDTO> chartData;
        private boolean chartData$set;

        public CustomerGrowthResponseBuilder() {
            super();
        }

        public CustomerGrowthResponseBuilder chartData(List<ChartPointDTO> chartData) {
            this.chartData = chartData;
            this.chartData$set = true;
            return this;
        }

        public CustomerGrowthResponse build() {
            return new CustomerGrowthResponse(
                this.chartData$set ? this.chartData : new ArrayList<ChartPointDTO>()
            );
        }
    }
}
