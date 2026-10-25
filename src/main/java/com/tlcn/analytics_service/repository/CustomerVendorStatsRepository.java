package com.tlcn.analytics_service.repository;

import com.tlcn.analytics_service.model.CustomerVendorStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerVendorStatsRepository extends JpaRepository<CustomerVendorStats, Long> {
    Optional<CustomerVendorStats> findByVendorKeycloakIdAndCustomerKeycloakId(
        String vendorKeycloakId,
        String customerKeycloakId
    );

    boolean existsByCustomerKeycloakId(String customerKeycloakId);

    long countByVendorKeycloakId(String vendorKeycloakId);

    @Query("SELECT COUNT(DISTINCT c.customerKeycloakId) FROM CustomerVendorStats c")
    long countDistinctCustomers();

    List<CustomerVendorStats> findByVendorKeycloakIdOrderByTotalSpentDesc(
        String vendorKeycloakId,
        Pageable pageable
    );

    @Query("""
        SELECT COUNT(c) FROM CustomerVendorStats c
        WHERE c.vendorKeycloakId = :vendorId
          AND c.firstOrderAt >= :from
          AND c.firstOrderAt < :to
        """)
    long countNewCustomersByVendor(
        @Param("vendorId") String vendorId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    List<CustomerVendorStats> findByVendorKeycloakId(String vendorKeycloakId);

    @Query("""
        SELECT c FROM CustomerVendorStats c
        WHERE c.vendorKeycloakId = :vendorId
          AND c.firstOrderAt IS NOT NULL
          AND c.firstOrderAt >= :from
          AND c.firstOrderAt < :to
        """)
    List<CustomerVendorStats> findByVendorAndFirstOrderBetween(
        @Param("vendorId") String vendorId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        SELECT c FROM CustomerVendorStats c
        WHERE c.vendorKeycloakId = :vendorId
          AND (
            :search IS NULL OR :search = '' OR
            LOWER(COALESCE(c.customerName, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR
            COALESCE(c.customerPhone, '') LIKE CONCAT('%', :search, '%')
          )
        """)
    Page<CustomerVendorStats> searchByVendor(
        @Param("vendorId") String vendorId,
        @Param("search") String search,
        Pageable pageable
    );
}
