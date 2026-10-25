package com.tlcn.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopCustomerDTO {
    private String customerKeycloakId;
    private String name;
    private String phone;
    private Long orders;
    private BigDecimal spent;

    public TopCustomerDTO() {
        super();
    }

    public TopCustomerDTO(
        String customerKeycloakId,
        String name,
        String phone,
        Long orders,
        BigDecimal spent
    ) {
        super();
        this.customerKeycloakId = customerKeycloakId;
        this.name = name;
        this.phone = phone;
        this.orders = orders;
        this.spent = spent;
    }

    public static TopCustomerDTOBuilder builder() {
        return new TopCustomerDTOBuilder();
    }

    public static class TopCustomerDTOBuilder {
        private String customerKeycloakId;
        private String name;
        private String phone;
        private Long orders;
        private BigDecimal spent;

        public TopCustomerDTOBuilder() {
            super();
        }

        public TopCustomerDTOBuilder customerKeycloakId(String customerKeycloakId) {
            this.customerKeycloakId = customerKeycloakId;
            return this;
        }

        public TopCustomerDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TopCustomerDTOBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public TopCustomerDTOBuilder orders(Long orders) {
            this.orders = orders;
            return this;
        }

        public TopCustomerDTOBuilder spent(BigDecimal spent) {
            this.spent = spent;
            return this;
        }

        public TopCustomerDTO build() {
            return new TopCustomerDTO(
                this.customerKeycloakId,
                this.name,
                this.phone,
                this.orders,
                this.spent
            );
        }
    }
}
