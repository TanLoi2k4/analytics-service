package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.dto.kafka.OrderCompletedEvent;
import com.tlcn.analytics_service.dto.kafka.OrderItemCompletedEvent;
import com.tlcn.analytics_service.dto.kafka.SubOrderCompletedEvent;
import com.tlcn.analytics_service.model.CategorySalesReport;
import com.tlcn.analytics_service.model.CustomerVendorStats;
import com.tlcn.analytics_service.model.DailySalesReport;
import com.tlcn.analytics_service.model.ProductPerformance;
import com.tlcn.analytics_service.model.VendorSalesReport;
import com.tlcn.analytics_service.repository.CategorySalesReportRepository;
import com.tlcn.analytics_service.repository.CustomerVendorStatsRepository;
import com.tlcn.analytics_service.repository.DailySalesReportRepository;
import com.tlcn.analytics_service.repository.ProductPerformanceRepository;
import com.tlcn.analytics_service.repository.VendorSalesReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AnalyticsAggregationService {

    private final DailySalesReportRepository dailySalesReportRepository;
    private final VendorSalesReportRepository vendorSalesReportRepository;
    private final ProductPerformanceRepository productPerformanceRepository;
    private final CustomerVendorStatsRepository customerVendorStatsRepository;
    private final CategorySalesReportRepository categorySalesReportRepository;
    private final AnalyticsCacheService analyticsCacheService;

    public AnalyticsAggregationService(
        DailySalesReportRepository dailySalesReportRepository,
        VendorSalesReportRepository vendorSalesReportRepository,
        ProductPerformanceRepository productPerformanceRepository,
        CustomerVendorStatsRepository customerVendorStatsRepository,
        CategorySalesReportRepository categorySalesReportRepository,
        AnalyticsCacheService analyticsCacheService
    ) {
        super();
        this.dailySalesReportRepository = dailySalesReportRepository;
        this.vendorSalesReportRepository = vendorSalesReportRepository;
        this.productPerformanceRepository = productPerformanceRepository;
        this.customerVendorStatsRepository = customerVendorStatsRepository;
        this.categorySalesReportRepository = categorySalesReportRepository;
        this.analyticsCacheService = analyticsCacheService;
    }
    @Transactional
    public void processOrderCompleted(OrderCompletedEvent event) {
        if (event == null) {
            return;
        }

        String eventId = resolveEventId(event);
        List<SubOrderCompletedEvent> subOrders = event.getSubOrders();
        if (subOrders == null || subOrders.isEmpty()) {
            log.warn("Skip order-completed without subOrders: orderCode={}", event.getOrderCode());
            return;
        }

        LocalDate reportDate = toReportDate(event.getCompletedAt());
        Instant completedAt = event.getCompletedAt() == null ? Instant.now() : event.getCompletedAt();
        String customerKeycloakId = event.getCustomerKeycloakId();
        boolean isNewCustomer = customerKeycloakId != null
            && !customerVendorStatsRepository.existsByCustomerKeycloakId(customerKeycloakId);

        BigDecimal orderGmv = BigDecimal.ZERO;
        BigDecimal orderCommission = BigDecimal.ZERO;
        long totalItemsSold = 0L;

        for (SubOrderCompletedEvent subOrder : subOrders) {
            BigDecimal subOrderRevenue = resolveSubOrderRevenue(subOrder);
            BigDecimal subOrderCommission = defaultZero(subOrder.getCommission());
            orderGmv = orderGmv.add(subOrderRevenue);
            orderCommission = orderCommission.add(subOrderCommission);

            long subOrderItems = updateVendorSalesReport(subOrder, reportDate, subOrderRevenue, subOrderCommission);
            totalItemsSold += subOrderItems;

            if (customerKeycloakId != null && subOrder.getVendorKeycloakId() != null) {
                updateCustomerVendorStats(
                    subOrder.getVendorKeycloakId(),
                    customerKeycloakId,
                    subOrderRevenue,
                    completedAt,
                    event.getCustomerName(),
                    event.getCustomerPhone()
                );
            }

            updateProductAndCategoryMetrics(subOrder, reportDate);
        }

        updateDailySalesReport(reportDate, orderGmv, isNewCustomer, orderCommission);

        analyticsCacheService.invalidateAfterOrderCompleted(collectVendorIds(subOrders));

        log.info(
            "Aggregated order-completed: eventId={}, orderCode={}, gmv={}, subOrders={}, items={}",
            eventId,
            event.getOrderCode(),
            orderGmv,
            subOrders.size(),
            totalItemsSold
        );
    }

    private Set<String> collectVendorIds(List<SubOrderCompletedEvent> subOrders) {
        Set<String> vendorIds = new LinkedHashSet<>();
        for (SubOrderCompletedEvent subOrder : subOrders) {
            String vendorId = subOrder.getVendorKeycloakId();
            if (vendorId != null && !vendorId.isBlank()) {
                vendorIds.add(vendorId);
            }
        }
        return vendorIds;
    }

    private void updateDailySalesReport(
        LocalDate reportDate,
        BigDecimal orderGmv,
        boolean isNewCustomer,
        BigDecimal orderCommission
    ) {
        DailySalesReport report = dailySalesReportRepository.findByReportDate(reportDate)
            .orElse(DailySalesReport.builder().reportDate(reportDate).build());

        report.setTotalRevenue(defaultZero(report.getTotalRevenue()).add(orderGmv));
        report.setTotalOrders(defaultZeroLong(report.getTotalOrders()) + 1L);
        report.setPlatformCommission(defaultZero(report.getPlatformCommission()).add(orderCommission));

        if (isNewCustomer) {
            report.setNewCustomers(defaultZeroLong(report.getNewCustomers()) + 1L);
        }

        dailySalesReportRepository.save(report);
    }

    private long updateVendorSalesReport(
        SubOrderCompletedEvent subOrder,
        LocalDate reportDate,
        BigDecimal subOrderRevenue,
        BigDecimal subOrderCommission
    ) {
        String vendorId = subOrder.getVendorKeycloakId();
        if (vendorId == null || vendorId.isBlank()) {
            return 0L;
        }

        VendorSalesReport report = vendorSalesReportRepository.findByVendorIdAndReportDate(vendorId, reportDate)
            .orElse(VendorSalesReport.builder()
                .vendorId(vendorId)
                .reportDate(reportDate)
                .build());

        long itemsSold = countItems(subOrder.getItems());

        report.setTotalRevenue(defaultZero(report.getTotalRevenue()).add(subOrderRevenue));
        report.setTotalOrders(defaultZeroLong(report.getTotalOrders()) + 1L);
        report.setTotalItemsSold(defaultZeroLong(report.getTotalItemsSold()) + itemsSold);
        report.setTotalCommission(defaultZero(report.getTotalCommission()).add(subOrderCommission));

        vendorSalesReportRepository.save(report);
        return itemsSold;
    }

    private void updateProductAndCategoryMetrics(SubOrderCompletedEvent subOrder, LocalDate reportDate) {
        if (subOrder.getItems() == null) {
            return;
        }

        String vendorId = subOrder.getVendorKeycloakId();

        for (OrderItemCompletedEvent item : subOrder.getItems()) {
            if (item.getProductId() == null) {
                continue;
            }

            BigDecimal lineTotal = defaultZero(item.getLineTotal());
            long quantity = defaultZeroLong(item.getQuantity());

            ProductPerformance performance = productPerformanceRepository.findByProductId(item.getProductId())
                .orElse(ProductPerformance.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .vendorId(vendorId)
                    .build());

            if (item.getProductName() != null && !item.getProductName().isBlank()) {
                performance.setProductName(item.getProductName());
            }
            if (vendorId != null) {
                performance.setVendorId(vendorId);
            }
            performance.setTotalQuantitySold(defaultZeroLong(performance.getTotalQuantitySold()) + quantity);
            performance.setTotalRevenue(defaultZero(performance.getTotalRevenue()).add(lineTotal));
            productPerformanceRepository.save(performance);

            String categoryName = normalizeCategory(item.getCategoryName());
            if (vendorId != null) {
                incrementCategorySales(vendorId, categoryName, reportDate, lineTotal, quantity);
            }
            incrementCategorySales(CategorySalesReport.PLATFORM_SCOPE, categoryName, reportDate, lineTotal, quantity);
        }
    }

    private void incrementCategorySales(
        String scopeKey,
        String categoryName,
        LocalDate reportDate,
        BigDecimal revenue,
        long quantity
    ) {
        CategorySalesReport report = categorySalesReportRepository
            .findByScopeKeyAndCategoryNameAndReportDate(scopeKey, categoryName, reportDate)
            .orElse(CategorySalesReport.builder()
                .scopeKey(scopeKey)
                .categoryName(categoryName)
                .reportDate(reportDate)
                .build());

        report.setRevenue(defaultZero(report.getRevenue()).add(revenue));
        report.setQuantitySold(defaultZeroLong(report.getQuantitySold()) + quantity);
        categorySalesReportRepository.save(report);
    }

    private void updateCustomerVendorStats(
        String vendorKeycloakId,
        String customerKeycloakId,
        BigDecimal spent,
        Instant completedAt,
        String customerName,
        String customerPhone
    ) {
        CustomerVendorStats stats = customerVendorStatsRepository
            .findByVendorKeycloakIdAndCustomerKeycloakId(vendorKeycloakId, customerKeycloakId)
            .orElse(CustomerVendorStats.builder()
                .vendorKeycloakId(vendorKeycloakId)
                .customerKeycloakId(customerKeycloakId)
                .firstOrderAt(completedAt)
                .build());

        stats.setTotalOrders(defaultZeroLong(stats.getTotalOrders()) + 1L);
        stats.setTotalSpent(defaultZero(stats.getTotalSpent()).add(spent));
        stats.setLastOrderAt(completedAt);
        if (stats.getFirstOrderAt() == null) {
            stats.setFirstOrderAt(completedAt);
        }
        if (customerName != null && !customerName.isBlank()) {
            stats.setCustomerName(customerName.trim());
        }
        if (customerPhone != null && !customerPhone.isBlank()) {
            stats.setCustomerPhone(customerPhone.trim());
        }

        customerVendorStatsRepository.save(stats);
    }

    private BigDecimal resolveSubOrderRevenue(SubOrderCompletedEvent subOrder) {
        if (subOrder.getSubTotal() != null && subOrder.getSubTotal().compareTo(BigDecimal.ZERO) > 0) {
            return subOrder.getSubTotal();
        }

        if (subOrder.getItems() == null) {
            return BigDecimal.ZERO;
        }

        return subOrder.getItems().stream()
            .map(item -> defaultZero(item.getLineTotal()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countItems(List<OrderItemCompletedEvent> items) {
        if (items == null) {
            return 0L;
        }
        return items.stream()
            .mapToLong(item -> defaultZeroLong(item.getQuantity()))
            .sum();
    }

    private String resolveEventId(OrderCompletedEvent event) {
        if (event.getEventId() != null && !event.getEventId().isBlank()) {
            return event.getEventId();
        }
        return "order-completed-" + event.getOrderCode();
    }

    private LocalDate toReportDate(Instant completedAt) {
        Instant instant = completedAt == null ? Instant.now() : completedAt;
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String normalizeCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            return "Uncategorized";
        }
        return categoryName.trim();
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long defaultZeroLong(Long value) {
        return value == null ? 0L : value;
    }
}
