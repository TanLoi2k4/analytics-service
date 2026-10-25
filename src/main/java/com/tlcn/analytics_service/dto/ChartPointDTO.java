package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChartPointDTO {
    private String date;
    private String name;
    private BigDecimal revenue;
    private Long orders;
    private Long value;

    public ChartPointDTO() {
        super();
    }

    public ChartPointDTO(
        String date,
        String name,
        BigDecimal revenue,
        Long orders,
        Long value
    ) {
        super();
        this.date = date;
        this.name = name;
        this.revenue = revenue;
        this.orders = orders;
        this.value = value;
    }

    public static ChartPointDTOBuilder builder() {
        return new ChartPointDTOBuilder();
    }

    public static class ChartPointDTOBuilder {
        private String date;
        private String name;
        private BigDecimal revenue;
        private Long orders;
        private Long value;

        public ChartPointDTOBuilder() {
            super();
        }

        public ChartPointDTOBuilder date(String date) {
            this.date = date;
            return this;
        }

        public ChartPointDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChartPointDTOBuilder revenue(BigDecimal revenue) {
            this.revenue = revenue;
            return this;
        }

        public ChartPointDTOBuilder orders(Long orders) {
            this.orders = orders;
            return this;
        }

        public ChartPointDTOBuilder value(Long value) {
            this.value = value;
            return this;
        }

        public ChartPointDTO build() {
            return new ChartPointDTO(
                this.date,
                this.name,
                this.revenue,
                this.orders,
                this.value
            );
        }
    }
}
