package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopVendorDTO {
    private String vendorId;
    private BigDecimal revenue;
    private Long orders;
    private Long itemsSold;
    private BigDecimal commission;

    public TopVendorDTO() {
        super();
    }

    public TopVendorDTO(
        String vendorId,
        BigDecimal revenue,
        Long orders,
        Long itemsSold,
        BigDecimal commission
    ) {
        super();
        this.vendorId = vendorId;
        this.revenue = revenue;
        this.orders = orders;
        this.itemsSold = itemsSold;
        this.commission = commission;
    }

    public static TopVendorDTOBuilder builder() {
        return new TopVendorDTOBuilder();
    }

    public static class TopVendorDTOBuilder {
        private String vendorId;
        private BigDecimal revenue;
        private Long orders;
        private Long itemsSold;
        private BigDecimal commission;

        public TopVendorDTOBuilder() {
            super();
        }

        public TopVendorDTOBuilder vendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public TopVendorDTOBuilder revenue(BigDecimal revenue) {
            this.revenue = revenue;
            return this;
        }

        public TopVendorDTOBuilder orders(Long orders) {
            this.orders = orders;
            return this;
        }

        public TopVendorDTOBuilder itemsSold(Long itemsSold) {
            this.itemsSold = itemsSold;
            return this;
        }

        public TopVendorDTOBuilder commission(BigDecimal commission) {
            this.commission = commission;
            return this;
        }

        public TopVendorDTO build() {
            return new TopVendorDTO(
                this.vendorId,
                this.revenue,
                this.orders,
                this.itemsSold,
                this.commission
            );
        }
    }
}
