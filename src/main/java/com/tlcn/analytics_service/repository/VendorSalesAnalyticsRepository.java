package com.tlcn.analytics_service.repository;

import com.tlcn.analytics_service.model.VendorSalesReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendorSalesAnalyticsRepository extends JpaRepository<VendorSalesReport, Long> {

    @Query("""
        SELECT COUNT(DISTINCT v.vendorId) FROM VendorSalesReport v
        WHERE v.reportDate BETWEEN :from AND :to
        """)
    long countDistinctVendors(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
        SELECT v.vendorId AS vendorId,
               SUM(v.totalRevenue) AS totalRevenue,
               SUM(v.totalOrders) AS totalOrders,
               SUM(v.totalItemsSold) AS totalItemsSold,
               SUM(v.totalCommission) AS totalCommission
        FROM VendorSalesReport v
        WHERE v.reportDate BETWEEN :from AND :to
        GROUP BY v.vendorId
        ORDER BY SUM(v.totalRevenue) DESC
        """)
    List<VendorRevenueAggregate> aggregateVendorRevenue(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    interface VendorRevenueAggregate {
        String getVendorId();
        BigDecimal getTotalRevenue();
        Long getTotalOrders();
        Long getTotalItemsSold();
        BigDecimal getTotalCommission();
    }
}
