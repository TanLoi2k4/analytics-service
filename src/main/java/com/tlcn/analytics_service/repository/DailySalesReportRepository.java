package com.tlcn.analytics_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tlcn.analytics_service.model.DailySalesReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySalesReportRepository extends JpaRepository<DailySalesReport, Long> {
    Optional<DailySalesReport> findByReportDate(LocalDate reportDate);
    List<DailySalesReport> findByReportDateBetweenOrderByReportDateAsc(LocalDate startDate, LocalDate endDate);
}