package com.tlcn.analytics_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "category_sales_reports",
    uniqueConstraints = @UniqueConstraint(columnNames = {"scope_key", "category_name", "report_date"})
)
@Getter
@Setter
public class CategorySalesReport {

    public static final String PLATFORM_SCOPE = "PLATFORM";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scope_key", nullable = false, updatable = false)
    private String scopeKey;

    @Column(name = "category_name", nullable = false, updatable = false)
    private String categoryName;

    @Column(name = "report_date", nullable = false, updatable = false)
    private LocalDate reportDate;

    private BigDecimal revenue = BigDecimal.ZERO;

    private Long quantitySold = 0L;

    public CategorySalesReport() {
        super();
        this.revenue = BigDecimal.ZERO;
        this.quantitySold = 0L;
    }

    public CategorySalesReport(
        Long id,
        String scopeKey,
        String categoryName,
        LocalDate reportDate,
        BigDecimal revenue,
        Long quantitySold
    ) {
        super();
        this.id = id;
        this.scopeKey = scopeKey;
        this.categoryName = categoryName;
        this.reportDate = reportDate;
        this.revenue = revenue;
        this.quantitySold = quantitySold;
    }

    public static CategorySalesReportBuilder builder() {
        return new CategorySalesReportBuilder();
    }

    public static class CategorySalesReportBuilder {
        private Long id;
        private String scopeKey;
        private String categoryName;
        private LocalDate reportDate;
        private BigDecimal revenue;
        private boolean revenue$set;
        private Long quantitySold;
        private boolean quantitySold$set;

        public CategorySalesReportBuilder() {
            super();
        }

        public CategorySalesReportBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CategorySalesReportBuilder scopeKey(String scopeKey) {
            this.scopeKey = scopeKey;
            return this;
        }

        public CategorySalesReportBuilder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public CategorySalesReportBuilder reportDate(LocalDate reportDate) {
            this.reportDate = reportDate;
            return this;
        }

        public CategorySalesReportBuilder revenue(BigDecimal revenue) {
            this.revenue = revenue;
            this.revenue$set = true;
            return this;
        }

        public CategorySalesReportBuilder quantitySold(Long quantitySold) {
            this.quantitySold = quantitySold;
            this.quantitySold$set = true;
            return this;
        }

        public CategorySalesReport build() {
            return new CategorySalesReport(
                this.id,
                this.scopeKey,
                this.categoryName,
                this.reportDate,
                this.revenue$set ? this.revenue : BigDecimal.ZERO,
                this.quantitySold$set ? this.quantitySold : 0L
            );
        }
    }
}
