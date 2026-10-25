package com.tlcn.analytics_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_sales_reports")
@Getter
@Setter
public class DailySalesReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private LocalDate reportDate;

    private BigDecimal totalRevenue = BigDecimal.ZERO;

    private Long totalOrders = 0L;

    private Long newCustomers = 0L;

    private BigDecimal platformCommission = BigDecimal.ZERO;

    public DailySalesReport() {
        super();
        this.totalRevenue = BigDecimal.ZERO;
        this.totalOrders = 0L;
        this.newCustomers = 0L;
        this.platformCommission = BigDecimal.ZERO;
    }

    public DailySalesReport(
        Long id,
        LocalDate reportDate,
        BigDecimal totalRevenue,
        Long totalOrders,
        Long newCustomers,
        BigDecimal platformCommission
    ) {
        super();
        this.id = id;
        this.reportDate = reportDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.newCustomers = newCustomers;
        this.platformCommission = platformCommission;
    }

    public static DailySalesReportBuilder builder() {
        return new DailySalesReportBuilder();
    }

    public static class DailySalesReportBuilder {
        private Long id;
        private LocalDate reportDate;
        private BigDecimal totalRevenue;
        private boolean totalRevenue$set;
        private Long totalOrders;
        private boolean totalOrders$set;
        private Long newCustomers;
        private boolean newCustomers$set;
        private BigDecimal platformCommission;
        private boolean platformCommission$set;

        public DailySalesReportBuilder() {
            super();
        }

        public DailySalesReportBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DailySalesReportBuilder reportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
            return this;
        }

        public DailySalesReportBuilder totalRevenue(BigDecimal totalRevenue) {
            this.totalRevenue = totalRevenue;
            this.totalRevenue$set = true;
            return this;
        }

        public DailySalesReportBuilder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            this.totalOrders$set = true;
            return this;
        }

        public DailySalesReportBuilder newCustomers(Long newCustomers) {
            this.newCustomers = newCustomers;
            this.newCustomers$set = true;
            return this;
        }

        public DailySalesReportBuilder platformCommission(BigDecimal platformCommission) {
            this.platformCommission = platformCommission;
            this.platformCommission$set = true;
            return this;
        }

        public DailySalesReport build() {
            return new DailySalesReport(
                this.id,
                this.reportDate,
                this.totalRevenue$set ? this.totalRevenue : BigDecimal.ZERO,
                this.totalOrders$set ? this.totalOrders : 0L,
                this.newCustomers$set ? this.newCustomers : 0L,
                this.platformCommission$set ? this.platformCommission : BigDecimal.ZERO
            );
        }
    }
}
