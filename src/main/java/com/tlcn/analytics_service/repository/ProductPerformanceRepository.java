package com.tlcn.analytics_service.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tlcn.analytics_service.model.ProductPerformance;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPerformanceRepository extends JpaRepository<ProductPerformance, Long> {
    Optional<ProductPerformance> findByProductId(Long productId);

    List<ProductPerformance> findByVendorIdOrderByTotalRevenueDesc(String vendorId, Pageable pageable);

    List<ProductPerformance> findAllByOrderByTotalRevenueDesc(Pageable pageable);
}