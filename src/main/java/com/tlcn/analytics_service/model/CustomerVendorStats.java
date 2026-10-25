package com.tlcn.analytics_service.model;

import com.tlcn.analytics_service.enums.CustomerSegment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "customer_vendor_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_keycloak_id", "customer_keycloak_id"})
)
@Getter
@Setter
public class CustomerVendorStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_keycloak_id", nullable = false, updatable = false)
    private String vendorKeycloakId;

    @Column(name = "customer_keycloak_id", nullable = false, updatable = false)
    private String customerKeycloakId;

    private Long totalOrders = 0L;

    private BigDecimal totalSpent = BigDecimal.ZERO;

    private Instant firstOrderAt;
    private Instant lastOrderAt;

    private String customerName;
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    private CustomerSegment segment;

    public CustomerVendorStats() {
        super();
        this.totalOrders = 0L;
        this.totalSpent = BigDecimal.ZERO;
    }

    public CustomerVendorStats(
        Long id,
        String vendorKeycloakId,
        String customerKeycloakId,
        Long totalOrders,
        BigDecimal totalSpent,
        Instant firstOrderAt,
        Instant lastOrderAt,
        String customerName,
        String customerPhone,
        CustomerSegment segment
    ) {
        super();
        this.id = id;
        this.vendorKeycloakId = vendorKeycloakId;
        this.customerKeycloakId = customerKeycloakId;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.firstOrderAt = firstOrderAt;
        this.lastOrderAt = lastOrderAt;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.segment = segment;
    }

    public static CustomerVendorStatsBuilder builder() {
        return new CustomerVendorStatsBuilder();
    }

    public static class CustomerVendorStatsBuilder {
        private Long id;
        private String vendorKeycloakId;
        private String customerKeycloakId;
        private Long totalOrders;
        private boolean totalOrders$set;
        private BigDecimal totalSpent;
        private boolean totalSpent$set;
        private Instant firstOrderAt;
        private Instant lastOrderAt;
        private String customerName;
        private String customerPhone;
        private CustomerSegment segment;

        public CustomerVendorStatsBuilder() {
            super();
        }

        public CustomerVendorStatsBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CustomerVendorStatsBuilder vendorKeycloakId(String vendorKeycloakId) {
            this.vendorKeycloakId = vendorKeycloakId;
            return this;
        }

        public CustomerVendorStatsBuilder customerKeycloakId(String customerKeycloakId) {
            this.customerKeycloakId = customerKeycloakId;
            return this;
        }

        public CustomerVendorStatsBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            this.totalOrders$set = true;
            return this;
        }

        public CustomerVendorStatsBuilder totalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent;
            this.totalSpent$set = true;
            return this;
        }

        public CustomerVendorStatsBuilder firstOrderAt(Instant firstOrderAt) {
            this.firstOrderAt = firstOrderAt;
            return this;
        }

        public CustomerVendorStatsBuilder lastOrderAt(Instant lastOrderAt) {
            this.lastOrderAt = lastOrderAt;
            return this;
        }

        public CustomerVendorStatsBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public CustomerVendorStatsBuilder customerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
            return this;
        }

        public CustomerVendorStatsBuilder segment(CustomerSegment segment) {
            this.segment = segment;
            return this;
        }

        public CustomerVendorStats build() {
            return new CustomerVendorStats(
                this.id,
                this.vendorKeycloakId,
                this.customerKeycloakId,
                this.totalOrders$set ? this.totalOrders : 0L,
                this.totalSpent$set ? this.totalSpent : BigDecimal.ZERO,
                this.firstOrderAt,
                this.lastOrderAt,
                this.customerName,
                this.customerPhone,
                this.segment
            );
        }
    }
}
