package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.enums.AnalyticsRange;
import com.tlcn.analytics_service.model.DailySalesReport;
import com.tlcn.analytics_service.model.VendorSalesReport;
import com.tlcn.analytics_service.repository.DailySalesReportRepository;
import com.tlcn.analytics_service.repository.VendorSalesAnalyticsRepository;
import com.tlcn.analytics_service.repository.VendorSalesReportRepository;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport.PeriodWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static com.tlcn.analytics_service.service.AnalyticsCacheService.*;
import static com.tlcn.analytics_service.util.AnalyticsMetricsSupport.*;

@Service
@Slf4j
public class GrowthCalculationService {

    private final DailySalesReportRepository dailySalesReportRepository;
    private final VendorSalesAnalyticsRepository vendorSalesAnalyticsRepository;
    private final VendorSalesReportRepository vendorSalesReportRepository;
    private final AnalyticsCacheService analyticsCacheService;

    public GrowthCalculationService(
        DailySalesReportRepository dailySalesReportRepository,
        VendorSalesAnalyticsRepository vendorSalesAnalyticsRepository,
        VendorSalesReportRepository vendorSalesReportRepository,
        AnalyticsCacheService analyticsCacheService
    ) {
        super();
        this.dailySalesReportRepository = dailySalesReportRepository;
        this.vendorSalesAnalyticsRepository = vendorSalesAnalyticsRepository;
        this.vendorSalesReportRepository = vendorSalesReportRepository;
        this.analyticsCacheService = analyticsCacheService;
    }
    public void refreshPlatformGrowthCache() {
        PeriodWindow period = resolvePeriod(AnalyticsRange.THIRTY_DAYS);
        List<DailySalesReport> current = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            period.from(), period.to()
        );
        List<DailySalesReport> previous = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            period.previousFrom(), period.previousTo()
        );

        double revenueGrowth = calculateGrowth(sumDailyRevenue(current), sumDailyRevenue(previous));
        double ordersGrowth = calculateGrowth(sumDailyOrders(current), sumDailyOrders(previous));

        analyticsCacheService.put(GROWTH_PLATFORM_REVENUE_KEY, revenueGrowth, Duration.ofHours(24));
        analyticsCacheService.put(GROWTH_PLATFORM_ORDERS_KEY, ordersGrowth, Duration.ofHours(24));

        vendorSalesAnalyticsRepository.aggregateVendorRevenue(period.from(), period.to())
            .forEach(row -> {
                List<VendorSalesReport> currentVendor = vendorSalesReportRepository
                    .findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
                        row.getVendorId(),
                        period.from(),
                        period.to()
                    );
                List<VendorSalesReport> previousVendor = vendorSalesReportRepository
                    .findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
                        row.getVendorId(),
                        period.previousFrom(),
                        period.previousTo()
                    );

                BigDecimal currentRevenue = sumVendorRevenue(currentVendor);
                BigDecimal previousRevenue = sumVendorRevenue(previousVendor);
                long currentOrders = sumVendorOrders(currentVendor);
                long previousOrders = sumVendorOrders(previousVendor);

                analyticsCacheService.put(
                    vendorRevenueGrowthKey(row.getVendorId()),
                    calculateGrowth(currentRevenue, previousRevenue),
                    Duration.ofHours(24)
                );
                analyticsCacheService.put(
                    vendorOrdersGrowthKey(row.getVendorId()),
                    calculateGrowth(currentOrders, previousOrders),
                    Duration.ofHours(24)
                );
            });

        log.info("Refreshed platform growth cache: revenue={}%, orders={}%", revenueGrowth, ordersGrowth);
    }
}
