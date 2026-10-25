package com.tlcn.analytics_service.dto;

import lombok.Data;

@Data
public class CustomerSegmentsResponse {
    private Long newCustomers;
    private Long regular;
    private Long vip;
    private Long inactive;

    public CustomerSegmentsResponse() {
        super();
    }

    public CustomerSegmentsResponse(
        Long newCustomers,
        Long regular,
        Long vip,
        Long inactive
    ) {
        super();
        this.newCustomers = newCustomers;
        this.regular = regular;
        this.vip = vip;
        this.inactive = inactive;
    }

    public static CustomerSegmentsResponseBuilder builder() {
        return new CustomerSegmentsResponseBuilder();
    }

    public static class CustomerSegmentsResponseBuilder {
        private Long newCustomers;
        private Long regular;
        private Long vip;
        private Long inactive;

        public CustomerSegmentsResponseBuilder() {
            super();
        }

        public CustomerSegmentsResponseBuilder newCustomers(Long newCustomers) {
            this.newCustomers = newCustomers;
            return this;
        }

        public CustomerSegmentsResponseBuilder regular(Long regular) {
            this.regular = regular;
            return this;
        }

        public CustomerSegmentsResponseBuilder vip(Long vip) {
            this.vip = vip;
            return this;
        }

        public CustomerSegmentsResponseBuilder inactive(Long inactive) {
            this.inactive = inactive;
            return this;
        }

        public CustomerSegmentsResponse build() {
            return new CustomerSegmentsResponse(
                this.newCustomers,
                this.regular,
                this.vip,
                this.inactive
            );
        }
    }
}
