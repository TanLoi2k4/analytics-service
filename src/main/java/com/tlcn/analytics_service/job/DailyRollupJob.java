package com.tlcn.analytics_service.job;

import com.tlcn.analytics_service.model.DailySalesReport;
import com.tlcn.analytics_service.model.VendorSalesReport;
import com.tlcn.analytics_service.repository.DailySalesReportRepository;
import com.tlcn.analytics_service.repository.VendorSalesReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.tlcn.analytics_service.util.AnalyticsMetricsSupport.*;

@Component
@Slf4j
public class DailyRollupJob {

    private final DailySalesReportRepository dailySalesReportRepository;
    private final VendorSalesReportRepository vendorSalesReportRepository;

    public DailyRollupJob(
        DailySalesReportRepository dailySalesReportRepository,
        VendorSalesReportRepository vendorSalesReportRepository
    ) {
        super();
        this.dailySalesReportRepository = dailySalesReportRepository;
        this.vendorSalesReportRepository = vendorSalesReportRepository;
    }
    @Scheduled(cron = "${analytics.jobs.daily-rollup-cron:0 0 1 * * ?}")
    public void reconcileYesterdayRollup() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<VendorSalesReport> vendorReports = vendorSalesReportRepository.findByReportDateBetween(yesterday, yesterday);

        if (vendorReports.isEmpty()) {
            Optional<DailySalesReport> existing = dailySalesReportRepository.findByReportDate(yesterday);
            if (existing.isPresent()) {
                log.info("Daily rollup verified for {} (no vendor data to reconcile)", yesterday);
            } else {
                log.info("No vendor sales data for {}, skipping daily rollup", yesterday);
            }
            return;
        }

        BigDecimal aggregatedRevenue = sumVendorRevenue(vendorReports);
        BigDecimal aggregatedCommission = sumVendorCommission(vendorReports);

        DailySalesReport report = dailySalesReportRepository.findByReportDate(yesterday)
            .orElseGet(() -> DailySalesReport.builder().reportDate(yesterday).build());

        boolean created = report.getId() == null;
        boolean revenueChanged = !zeroIfNull(report.getTotalRevenue()).equals(aggregatedRevenue);
        boolean commissionChanged = !zeroIfNull(report.getPlatformCommission()).equals(aggregatedCommission);

        if (!created && !revenueChanged && !commissionChanged) {
            log.info("Daily rollup verified for {}", yesterday);
            return;
        }

        report.setTotalRevenue(aggregatedRevenue);
        report.setPlatformCommission(aggregatedCommission);
        dailySalesReportRepository.save(report);

        if (created) {
            log.warn(
                "Created daily rollup from vendor aggregation for {}: revenue={}, commission={}",
                yesterday, aggregatedRevenue, aggregatedCommission
            );
        } else {
            log.warn(
                "Reconciled daily rollup for {} from vendor aggregation: revenue={}, commission={}",
                yesterday, aggregatedRevenue, aggregatedCommission
            );
        }
    }
}
