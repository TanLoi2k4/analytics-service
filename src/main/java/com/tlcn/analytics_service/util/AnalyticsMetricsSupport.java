package com.tlcn.analytics_service.util;

import com.tlcn.analytics_service.enums.AnalyticsRange;
import com.tlcn.analytics_service.model.DailySalesReport;
import com.tlcn.analytics_service.model.VendorSalesReport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public final class AnalyticsMetricsSupport {

    private AnalyticsMetricsSupport() {
    }

    public record PeriodWindow(LocalDate from, LocalDate to, LocalDate previousFrom, LocalDate previousTo) {
    }

    public static PeriodWindow resolvePeriod(AnalyticsRange range) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(range.getDays() - 1L);
        long span = range.getDays();
        LocalDate previousTo = from.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(span - 1);
        return new PeriodWindow(from, to, previousFrom, previousTo);
    }

    public static PeriodWindow resolveCustomPeriod(LocalDate from, LocalDate to) {
        LocalDate safeFrom = from == null ? LocalDate.now().minusDays(29) : from;
        LocalDate safeTo = to == null ? LocalDate.now() : to;
        if (safeFrom.isAfter(safeTo)) {
            LocalDate tmp = safeFrom;
            safeFrom = safeTo;
            safeTo = tmp;
        }
        long span = safeTo.toEpochDay() - safeFrom.toEpochDay() + 1;
        LocalDate previousTo = safeFrom.minusDays(1);
        LocalDate previousFrom = previousTo.minusDays(span - 1);
        return new PeriodWindow(safeFrom, safeTo, previousFrom, previousTo);
    }

    public static BigDecimal sumDailyRevenue(List<DailySalesReport> reports) {
        return reports.stream()
            .map(DailySalesReport::getTotalRevenue)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static long sumDailyOrders(List<DailySalesReport> reports) {
        return reports.stream()
            .mapToLong(report -> nullSafeLong(report.getTotalOrders()))
            .sum();
    }

    public static BigDecimal sumDailyCommission(List<DailySalesReport> reports) {
        return reports.stream()
            .map(DailySalesReport::getPlatformCommission)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static long sumDailyNewCustomers(List<DailySalesReport> reports) {
        return reports.stream()
            .mapToLong(report -> nullSafeLong(report.getNewCustomers()))
            .sum();
    }

    public static BigDecimal sumVendorRevenue(List<VendorSalesReport> reports) {
        return reports.stream()
            .map(VendorSalesReport::getTotalRevenue)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static long sumVendorOrders(List<VendorSalesReport> reports) {
        return reports.stream()
            .mapToLong(report -> nullSafeLong(report.getTotalOrders()))
            .sum();
    }

    public static BigDecimal sumVendorCommission(List<VendorSalesReport> reports) {
        return reports.stream()
            .map(VendorSalesReport::getTotalCommission)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal avgOrderValue(BigDecimal revenue, long orders) {
        if (orders <= 0) {
            return BigDecimal.ZERO;
        }
        return revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
    }

    public static double calculateGrowth(BigDecimal current, BigDecimal previous) {
        BigDecimal safeCurrent = zeroIfNull(current);
        BigDecimal safePrevious = zeroIfNull(previous);
        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            return safeCurrent.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return safeCurrent.subtract(safePrevious)
            .divide(safePrevious, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    public static double calculateGrowth(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    public static Double growthOrNull(BigDecimal current, BigDecimal previous) {
        BigDecimal safePrevious = zeroIfNull(previous);
        if (safePrevious.compareTo(BigDecimal.ZERO) == 0) {
            return zeroIfNull(current).compareTo(BigDecimal.ZERO) == 0 ? 0.0 : null;
        }
        return calculateGrowth(current, previous);
    }

    public static Double growthOrNull(long current, long previous) {
        if (previous <= 0) {
            return current == 0 ? 0.0 : null;
        }
        return calculateGrowth(current, previous);
    }

    public static BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }
}
