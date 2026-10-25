package com.tlcn.analytics_service.dto.kafka;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubOrderCompletedEvent {
    private Long subOrderId;
    private String vendorKeycloakId;
    private BigDecimal subTotal;
    private BigDecimal commission;
    private BigDecimal vendorPayout;

    private List<OrderItemCompletedEvent> items = new ArrayList<>();

    public SubOrderCompletedEvent() {
        super();
        this.items = new ArrayList<>();
    }

    public SubOrderCompletedEvent(
        Long subOrderId,
        String vendorKeycloakId,
        BigDecimal subTotal,
        BigDecimal commission,
        BigDecimal vendorPayout,
        List<OrderItemCompletedEvent> items
    ) {
        super();
        this.subOrderId = subOrderId;
        this.vendorKeycloakId = vendorKeycloakId;
        this.subTotal = subTotal;
        this.commission = commission;
        this.vendorPayout = vendorPayout;
        this.items = items;
    }

    public static SubOrderCompletedEventBuilder builder() {
        return new SubOrderCompletedEventBuilder();
    }

    public static class SubOrderCompletedEventBuilder {
        private Long subOrderId;
        private String vendorKeycloakId;
        private BigDecimal subTotal;
        private BigDecimal commission;
        private BigDecimal vendorPayout;
        private List<OrderItemCompletedEvent> items;
        private boolean items$set;

        public SubOrderCompletedEventBuilder() {
            super();
        }

        public SubOrderCompletedEventBuilder subOrderId(Long subOrderId) {
            this.subOrderId = subOrderId;
            return this;
        }

        public SubOrderCompletedEventBuilder vendorKeycloakId(String vendorKeycloakId) {
            this.vendorKeycloakId = vendorKeycloakId;
            return this;
        }

        public SubOrderCompletedEventBuilder subTotal(BigDecimal subTotal) {
            this.subTotal = subTotal;
            return this;
        }

        public SubOrderCompletedEventBuilder commission(BigDecimal commission) {
            this.commission = commission;
            return this;
        }

        public SubOrderCompletedEventBuilder vendorPayout(BigDecimal vendorPayout) {
            this.vendorPayout = vendorPayout;
            return this;
        }

        public SubOrderCompletedEventBuilder items(List<OrderItemCompletedEvent> items) {
            this.items = items;
            this.items$set = true;
            return this;
        }

        public SubOrderCompletedEvent build() {
            return new SubOrderCompletedEvent(
                this.subOrderId,
                this.vendorKeycloakId,
                this.subTotal,
                this.commission,
                this.vendorPayout,
                this.items$set ? this.items : new ArrayList<OrderItemCompletedEvent>()
            );
        }
    }
}
