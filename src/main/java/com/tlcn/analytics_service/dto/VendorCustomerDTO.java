package com.tlcn.analytics_service.dto;

import com.tlcn.analytics_service.enums.CustomerSegment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VendorCustomerDTO {
    private String customerKeycloakId;
    private String name;
    private String phone;
    private Long orders;
    private BigDecimal spent;
    private Instant firstOrderAt;
    private Instant lastOrderAt;
    private CustomerSegment segment;

    public VendorCustomerDTO() {
        super();
    }

    public VendorCustomerDTO(
        String customerKeycloakId,
        String name,
        String phone,
        Long orders,
        BigDecimal spent,
        Instant firstOrderAt,
        Instant lastOrderAt,
        CustomerSegment segment
    ) {
        super();
        this.customerKeycloakId = customerKeycloakId;
        this.name = name;
        this.phone = phone;
        this.orders = orders;
        this.spent = spent;
        this.firstOrderAt = firstOrderAt;
        this.lastOrderAt = lastOrderAt;
        this.segment = segment;
    }

    public static VendorCustomerDTOBuilder builder() {
        return new VendorCustomerDTOBuilder();
    }

    public static class VendorCustomerDTOBuilder {
        private String customerKeycloakId;
        private String name;
        private String phone;
        private Long orders;
        private BigDecimal spent;
        private Instant firstOrderAt;
        private Instant lastOrderAt;
        private CustomerSegment segment;

        public VendorCustomerDTOBuilder() {
            super();
        }

        public VendorCustomerDTOBuilder customerKeycloakId(String customerKeycloakId) {
            this.customerKeycloakId = customerKeycloakId;
            return this;
        }

        public VendorCustomerDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VendorCustomerDTOBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public VendorCustomerDTOBuilder orders(Long orders) {
            this.orders = orders;
            return this;
        }

        public VendorCustomerDTOBuilder spent(BigDecimal spent) {
            this.spent = spent;
            return this;
        }

        public VendorCustomerDTOBuilder firstOrderAt(Instant firstOrderAt) {
            this.firstOrderAt = firstOrderAt;
            return this;
        }

        public VendorCustomerDTOBuilder lastOrderAt(Instant lastOrderAt) {
            this.lastOrderAt = lastOrderAt;
            return this;
        }

        public VendorCustomerDTOBuilder segment(CustomerSegment segment) {
            this.segment = segment;
            return this;
        }

        public VendorCustomerDTO build() {
            return new VendorCustomerDTO(
                this.customerKeycloakId,
                this.name,
                this.phone,
                this.orders,
                this.spent,
                this.firstOrderAt,
                this.lastOrderAt,
                this.segment
            );
        }
    }
}
