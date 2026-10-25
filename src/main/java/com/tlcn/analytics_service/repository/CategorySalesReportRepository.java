package com.tlcn.analytics_service.repository;

import com.tlcn.analytics_service.model.CategorySalesReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategorySalesReportRepository extends JpaRepository<CategorySalesReport, Long> {
    Optional<CategorySalesReport> findByScopeKeyAndCategoryNameAndReportDate(
        String scopeKey,
        String categoryName,
        LocalDate reportDate
    );

    List<CategorySalesReport> findByScopeKeyAndReportDateBetween(
        String scopeKey,
        LocalDate startDate,
        LocalDate endDate
    );
}
