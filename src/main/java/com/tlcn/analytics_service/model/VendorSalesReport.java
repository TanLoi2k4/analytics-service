package com.tlcn.analytics_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "vendor_sales_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vendorId", "reportDate"})
)
@Getter
@Setter
public class VendorSalesReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String vendorId;

    @Column(nullable = false, updatable = false)
    private LocalDate reportDate;

    private BigDecimal totalRevenue = BigDecimal.ZERO;

    private Long totalOrders = 0L;

    private Long totalItemsSold = 0L;

    private BigDecimal totalCommission = BigDecimal.ZERO;

    public VendorSalesReport() {
        super();
        this.totalRevenue = BigDecimal.ZERO;
        this.totalOrders = 0L;
        this.totalItemsSold = 0L;
        this.totalCommission = BigDecimal.ZERO;
    }

    public VendorSalesReport(
        Long id,
        String vendorId,
        LocalDate reportDate,
        BigDecimal totalRevenue,
        Long totalOrders,
        Long totalItemsSold,
        BigDecimal totalCommission
    ) {
        super();
        this.id = id;
        this.vendorId = vendorId;
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalItemsSold = totalItemsSold;
        this.totalCommission = totalCommission;
    }

    public static VendorSalesReportBuilder builder() {
        return new VendorSalesReportBuilder();
    }

    public static class VendorSalesReportBuilder {
        private Long id;
        private String vendorId;
        private LocalDate reportDate;
        private BigDecimal totalRevenue;
        private boolean totalRevenue$set;
        private Long totalOrders;
        private boolean totalOrders$set;
        private Long totalItemsSold;
        private boolean totalItemsSold$set;
        private BigDecimal totalCommission;
        private boolean totalCommission$set;

        public VendorSalesReportBuilder() {
            super();
        }

        public VendorSalesReportBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VendorSalesReportBuilder vendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public VendorSalesReportBuilder reportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
            return this;
        }

        public VendorSalesReportBuilder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            this.totalRevenue$set = true;
            return this;
        }

        public VendorSalesReportBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            this.totalOrders$set = true;
            return this;
        }

        public VendorSalesReportBuilder totalItemsSold(Long totalItemsSold) {
            this.totalItemsSold = totalItemsSold;
            this.totalItemsSold$set = true;
            return this;
        }

        public VendorSalesReportBuilder totalCommission(BigDecimal totalCommission) {
            this.totalCommission = totalCommission;
            this.totalCommission$set = true;
            return this;
        }

        public VendorSalesReport build() {
            return new VendorSalesReport(
                this.id,
                this.vendorId,
                this.reportDate,
                this.totalRevenue$set ? this.totalRevenue : BigDecimal.ZERO,
                this.totalOrders$set ? this.totalOrders : 0L,
                this.totalItemsSold$set ? this.totalItemsSold : 0L,
                this.totalCommission$set ? this.totalCommission : BigDecimal.ZERO
            );
        }
    }
}