package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryBreakdownDTO {
    private String name;
    private BigDecimal revenue;
    private Long quantitySold;
    private Double value;

    public CategoryBreakdownDTO() {
        super();
    }

    public CategoryBreakdownDTO(
        String name,
        BigDecimal revenue,
        Long quantitySold,
        Double value
    ) {
        super();
        this.name = name;
        this.revenue = revenue;
        this.quantitySold = quantitySold;
        this.value = value;
    }

    public static CategoryBreakdownDTOBuilder builder() {
        return new CategoryBreakdownDTOBuilder();
    }

    public static class CategoryBreakdownDTOBuilder {
        private String name;
        private BigDecimal revenue;
        private Long quantitySold;
        private Double value;

        public CategoryBreakdownDTOBuilder() {
            super();
        }

        public CategoryBreakdownDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CategoryBreakdownDTOBuilder revenue(BigDecimal revenue) {
            this.revenue = revenue;
            return this;
        }

        public CategoryBreakdownDTOBuilder quantitySold(Long quantitySold) {
            this.quantitySold = quantitySold;
            return this;
        }

        public CategoryBreakdownDTOBuilder value(Double value) {
            this.value = value;
            return this;
        }

        public CategoryBreakdownDTO build() {
            return new CategoryBreakdownDTO(
                this.name,
                this.revenue,
                this.quantitySold,
                this.value
            );
        }
    }
}
