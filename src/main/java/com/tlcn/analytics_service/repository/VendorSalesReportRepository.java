package com.tlcn.analytics_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tlcn.analytics_service.model.VendorSalesReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorSalesReportRepository extends JpaRepository<VendorSalesReport, Long> {
    Optional<VendorSalesReport> findByVendorIdAndReportDate(String vendorId, LocalDate reportDate);

    List<VendorSalesReport> findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
        String vendorId,
        LocalDate startDate,
        LocalDate endDate
    );

    List<VendorSalesReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);
}