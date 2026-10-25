package com.tlcn.analytics_service.dto.kafka;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderCompletedEvent {
    private String eventId;
    private String orderCode;
    private Long orderId;
    private String customerKeycloakId;
    private String customerName;
    private String customerPhone;
    private Instant completedAt;

    private List<SubOrderCompletedEvent> subOrders = new ArrayList<>();

    public OrderCompletedEvent() {
        super();
        this.subOrders = new ArrayList<>();
    }

    public OrderCompletedEvent(
        String eventId,
        String orderCode,
        Long orderId,
        String customerKeycloakId,
        String customerName,
        String customerPhone,
        Instant completedAt,
        List<SubOrderCompletedEvent> subOrders
    ) {
        super();
        this.eventId = eventId;
        this.orderCode = orderCode;
        this.orderId = orderId;
        this.customerKeycloakId = customerKeycloakId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.completedAt = completedAt;
        this.subOrders = subOrders;
    }

    public static OrderCompletedEventBuilder builder() {
        return new OrderCompletedEventBuilder();
    }

    public static class OrderCompletedEventBuilder {
        private String eventId;
        private String orderCode;
        private Long orderId;
        private String customerKeycloakId;
        private String customerName;
        private String customerPhone;
        private Instant completedAt;
        private List<SubOrderCompletedEvent> subOrders;
        private boolean subOrders$set;

        public OrderCompletedEventBuilder() {
            super();
        }

        public OrderCompletedEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public OrderCompletedEventBuilder orderCode(String orderCode) {
            this.orderCode = orderCode;
            return this;
        }

        public OrderCompletedEventBuilder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderCompletedEventBuilder customerKeycloakId(String customerKeycloakId) {
            this.customerKeycloakId = customerKeycloakId;
            return this;
        }

        public OrderCompletedEventBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public OrderCompletedEventBuilder customerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
            return this;
        }

        public OrderCompletedEventBuilder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public OrderCompletedEventBuilder subOrders(List<SubOrderCompletedEvent> subOrders) {
            this.subOrders = subOrders;
            this.subOrders$set = true;
            return this;
        }

        public OrderCompletedEvent build() {
            return new OrderCompletedEvent(
                this.eventId,
                this.orderCode,
                this.orderId,
                this.customerKeycloakId,
                this.customerName,
                this.customerPhone,
                this.completedAt,
                this.subOrders$set ? this.subOrders : new ArrayList<SubOrderCompletedEvent>()
            );
        }
    }
}
