package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.client.OrderClient;
import com.tlcn.analytics_service.client.ProductClient;
import com.tlcn.analytics_service.dto.*;
import com.tlcn.analytics_service.dto.external.ProductSalesRowDto;
import com.tlcn.analytics_service.enums.AnalyticsRange;
import com.tlcn.analytics_service.enums.StatsPeriod;
import com.tlcn.analytics_service.model.CategorySalesReport;
import com.tlcn.analytics_service.model.DailySalesReport;
import com.tlcn.analytics_service.model.ProductPerformance;
import com.tlcn.analytics_service.model.VendorSalesReport;
import com.tlcn.analytics_service.repository.CategorySalesReportRepository;
import com.tlcn.analytics_service.repository.CustomerVendorStatsRepository;
import com.tlcn.analytics_service.repository.DailySalesReportRepository;
import com.tlcn.analytics_service.repository.ProductPerformanceRepository;
import com.tlcn.analytics_service.repository.VendorSalesAnalyticsRepository;
import com.tlcn.analytics_service.repository.VendorSalesReportRepository;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport.PeriodWindow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tlcn.analytics_service.model.CategorySalesReport.PLATFORM_SCOPE;
import static com.tlcn.analytics_service.util.AnalyticsMetricsSupport.*;

@Service
public class AdminAnalyticsService {

    private final DailySalesReportRepository dailySalesReportRepository;
    private final VendorSalesReportRepository vendorSalesReportRepository;
    private final VendorSalesAnalyticsRepository vendorSalesAnalyticsRepository;
    private final ProductPerformanceRepository productPerformanceRepository;
    private final CategorySalesReportRepository categorySalesReportRepository;
    private final CustomerVendorStatsRepository customerVendorStatsRepository;
    private final AnalyticsCacheService analyticsCacheService;
    private final OrderClient orderClient;
    private final ProductClient productClient;

    public AdminAnalyticsService(
        DailySalesReportRepository dailySalesReportRepository,
        VendorSalesReportRepository vendorSalesReportRepository,
        VendorSalesAnalyticsRepository vendorSalesAnalyticsRepository,
        ProductPerformanceRepository productPerformanceRepository,
        CategorySalesReportRepository categorySalesReportRepository,
        CustomerVendorStatsRepository customerVendorStatsRepository,
        AnalyticsCacheService analyticsCacheService,
        OrderClient orderClient,
        ProductClient productClient
    ) {
        super();
        this.dailySalesReportRepository = dailySalesReportRepository;
        this.vendorSalesReportRepository = vendorSalesReportRepository;
        this.vendorSalesAnalyticsRepository = vendorSalesAnalyticsRepository;
        this.productPerformanceRepository = productPerformanceRepository;
        this.categorySalesReportRepository = categorySalesReportRepository;
        this.customerVendorStatsRepository = customerVendorStatsRepository;
        this.analyticsCacheService = analyticsCacheService;
        this.orderClient = orderClient;
        this.productClient = productClient;
    }
    public AdminDashboardResponse getDashboard(StatsPeriod period, LocalDate anchor) {
        String cacheKey = AnalyticsCacheService.adminDashboardKey(
            StatsPeriodSupport.cacheSuffix(period, anchor)
        );
        return analyticsCacheService.get(cacheKey, AdminDashboardResponse.class)
            .orElseGet(() -> {
                AdminDashboardResponse response = buildDashboard(period, anchor);
                analyticsCacheService.putDashboard(cacheKey, response);
                return response;
            });
    }

    public SalesAnalyticsResponse getRevenueChart(String rangeCode) {
        AnalyticsRange range = AnalyticsRange.fromCode(rangeCode);
        PeriodWindow period = resolvePeriod(range);
        return buildRevenueChart(period, null);
    }

    public SalesAnalyticsResponse getRevenueChart(StatsPeriod period, LocalDate anchor) {
        StatsPeriodSupport.ResolvedPeriod resolved = StatsPeriodSupport.resolve(period, anchor);
        return buildRevenueChart(resolved.toWindow(), resolved);
    }

    public RevenueReportResponse getRevenueReport(StatsPeriod period, LocalDate anchor) {
        StatsPeriodSupport.ResolvedPeriod resolved = StatsPeriodSupport.resolve(period, anchor);
        PeriodWindow window = resolved.toWindow();
        List<DailySalesReport> reports = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.from(), window.to()
        );
        List<DailySalesReport> previousReports = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.previousFrom(), window.previousTo()
        );

        BigDecimal gmv = sumDailyRevenue(reports);
        long orders = sumDailyOrders(reports);
        BigDecimal commission = sumDailyCommission(reports);
        BigDecimal previousGmv = sumDailyRevenue(previousReports);
        boolean comparable = isComparable(window);

        SummaryStatsDTO summary = SummaryStatsDTO.builder()
            .totalRevenue(gmv)
            .totalOrders(orders)
            .avgOrderValue(avgOrderValue(gmv, orders))
            .commission(commission)
            .netRevenue(gmv.subtract(commission).max(BigDecimal.ZERO))
            .growth(comparable ? growthOrNull(gmv, previousGmv) : null)
            .ordersGrowth(comparable ? growthOrNull(orders, sumDailyOrders(previousReports)) : null)
            .build();

        return RevenueReportResponse.builder()
            .summary(summary)
            .chartData(buildAggregatedData(reports, resolved, window))
            .categoryData(getProductsByCategoryForPeriod(window))
            .build();
    }

    public List<TopVendorDTO> getTopVendors(int limit, StatsPeriod period, LocalDate anchor) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        return vendorSalesAnalyticsRepository.aggregateVendorRevenue(window.from(), window.to())
            .stream()
            .limit(Math.max(limit, 1))
            .map(row -> TopVendorDTO.builder()
                .vendorId(row.getVendorId())
                .revenue(zeroIfNull(row.getTotalRevenue()))
                .orders(nullSafeLong(row.getTotalOrders()))
                .itemsSold(nullSafeLong(row.getTotalItemsSold()))
                .commission(zeroIfNull(row.getTotalCommission()))
                .build())
            .toList();
    }

    public VendorDetailAnalyticsResponse getVendorDetail(String vendorId, StatsPeriod period, LocalDate anchor) {
        StatsPeriodSupport.ResolvedPeriod resolved = StatsPeriodSupport.resolve(period, anchor);
        PeriodWindow window = resolved.toWindow();
        List<VendorSalesReport> reports = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorId, window.from(), window.to()
        );
        List<VendorSalesReport> previous = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorId, window.previousFrom(), window.previousTo()
        );

        BigDecimal revenue = sumVendorRevenue(reports);
        long orders = sumVendorOrders(reports);
        boolean comparable = isComparable(window);

        SummaryStatsDTO summary = SummaryStatsDTO.builder()
            .totalRevenue(revenue)
            .totalOrders(orders)
            .avgOrderValue(avgOrderValue(revenue, orders))
            .commission(sumVendorCommission(reports))
            .growth(comparable ? growthOrNull(revenue, sumVendorRevenue(previous)) : null)
            .ordersGrowth(comparable ? growthOrNull(orders, sumVendorOrders(previous)) : null)
            .build();

        List<ChartPointDTO> chart = buildVendorAggregatedData(reports, resolved, window);

        return VendorDetailAnalyticsResponse.builder()
            .vendorId(vendorId)
            .summary(summary)
            .revenue(chart)
            .orders(chart)
            .topProducts(getVendorTopProducts(vendorId, period, window, 5))
            .build();
    }

    public List<TopProductDTO> getTopProducts(int limit, StatsPeriod period, LocalDate anchor) {
        int safeLimit = Math.max(limit, 1);
        StatsPeriod safePeriod = period == null ? StatsPeriod.MONTH : period;
        if (safePeriod == StatsPeriod.ALL) {
            return fromProductPerformance(safeLimit);
        }

        PeriodWindow window = StatsPeriodSupport.resolve(safePeriod, anchor).toWindow();
        List<ProductSalesRowDto> rows = orderClient.getPlatformProductSales(window.from(), window.to(), safeLimit);
        if (rows == null) {
            return fromProductPerformance(safeLimit);
        }
        return enrichTopProducts(rows.stream()
            .filter(row -> row.getProductId() != null)
            .map(this::toTopProduct)
            .toList());
    }

    public List<CategoryBreakdownDTO> getProductsByCategory(StatsPeriod period, LocalDate anchor) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        return getProductsByCategoryForPeriod(window);
    }

    private AdminDashboardResponse buildDashboard(StatsPeriod period, LocalDate anchor) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        List<DailySalesReport> current = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.from(), window.to()
        );
        List<DailySalesReport> previous = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.previousFrom(), window.previousTo()
        );

        BigDecimal gmv = sumDailyRevenue(current);
        long orders = sumDailyOrders(current);
        long previousOrders = sumDailyOrders(previous);
        long newCustomers = sumDailyNewCustomers(current);
        long previousNewCustomers = sumDailyNewCustomers(previous);
        boolean comparable = isComparable(window);

        return AdminDashboardResponse.builder()
            .gmv(gmv)
            .totalOrders(orders)
            .totalUsers(customerVendorStatsRepository.countDistinctCustomers())
            .totalVendors(vendorSalesAnalyticsRepository.countDistinctVendors(window.from(), window.to()))
            .gmvGrowth(comparable ? calculateGrowth(gmv, sumDailyRevenue(previous)) : null)
            .ordersGrowth(comparable ? calculateGrowth(orders, previousOrders) : null)
            .userGrowth(comparable ? calculateGrowth(newCustomers, previousNewCustomers) : null)
            .commissionEarned(sumDailyCommission(current))
            .build();
    }

    private SalesAnalyticsResponse buildRevenueChart(PeriodWindow window, StatsPeriodSupport.ResolvedPeriod resolved) {
        List<DailySalesReport> current = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.from(), window.to()
        );
        List<DailySalesReport> previous = dailySalesReportRepository.findByReportDateBetweenOrderByReportDateAsc(
            window.previousFrom(), window.previousTo()
        );

        BigDecimal revenue = sumDailyRevenue(current);
        long orders = sumDailyOrders(current);
        boolean comparable = isComparable(window);

        SummaryStatsDTO summary = SummaryStatsDTO.builder()
            .totalRevenue(revenue)
            .totalOrders(orders)
            .avgOrderValue(avgOrderValue(revenue, orders))
            .growth(comparable ? growthOrNull(revenue, sumDailyRevenue(previous)) : null)
            .ordersGrowth(comparable ? growthOrNull(orders, sumDailyOrders(previous)) : null)
            .commission(sumDailyCommission(current))
            .build();

        List<ChartPointDTO> chartData = resolved == null
            ? current.stream()
                .map(report -> ChartPointDTO.builder()
                    .date(report.getReportDate().toString())
                    .name(report.getReportDate().toString())
                    .revenue(zeroIfNull(report.getTotalRevenue()))
                    .orders(nullSafeLong(report.getTotalOrders()))
                    .build())
                .toList()
            : buildFilledChartPoints(current, window);
        List<ChartPointDTO> weeklyData = resolved == null
            ? List.of()
            : buildAggregatedData(current, resolved, window);

        return SalesAnalyticsResponse.builder()
            .summary(summary)
            .chartData(chartData)
            .weeklyData(weeklyData)
            .build();
    }

    private List<CategoryBreakdownDTO> getProductsByCategoryForPeriod(PeriodWindow window) {
        List<CategorySalesReport> reports = categorySalesReportRepository.findByScopeKeyAndReportDateBetween(
            PLATFORM_SCOPE,
            window.from(),
            window.to()
        );
        return aggregateCategories(reports);
    }

    private List<TopProductDTO> getVendorTopProducts(
        String vendorId,
        StatsPeriod period,
        PeriodWindow window,
        int limit
    ) {
        StatsPeriod safePeriod = period == null ? StatsPeriod.MONTH : period;
        if (safePeriod == StatsPeriod.ALL) {
            return fromVendorProductPerformance(vendorId, limit);
        }

        List<ProductSalesRowDto> rows = orderClient.getVendorProductSales(
            vendorId,
            window.from(),
            window.to(),
            limit
        );
        if (rows == null) {
            return fromVendorProductPerformance(vendorId, limit);
        }
        return enrichTopProducts(rows.stream()
            .filter(row -> row.getProductId() != null)
            .map(this::toTopProduct)
            .toList());
    }

    private List<TopProductDTO> fromProductPerformance(int limit) {
        return enrichTopProducts(productPerformanceRepository
            .findAllByOrderByTotalRevenueDesc(PageRequest.of(0, limit))
            .stream()
            .map(this::toTopProduct)
            .toList());
    }

    private List<TopProductDTO> fromVendorProductPerformance(String vendorId, int limit) {
        return enrichTopProducts(productPerformanceRepository
            .findByVendorIdOrderByTotalRevenueDesc(vendorId, PageRequest.of(0, limit))
            .stream()
            .map(this::toTopProduct)
            .toList());
    }

    private List<TopProductDTO> enrichTopProducts(List<TopProductDTO> products) {
        return products.stream().map(productClient::enrich).toList();
    }

    private TopProductDTO toTopProduct(ProductPerformance product) {
        long qty = nullSafeLong(product.getTotalQuantitySold());
        return TopProductDTO.builder()
            .productId(product.getProductId())
            .name(product.getProductName())
            .quantity(qty)
            .sales(qty)
            .revenue(zeroIfNull(product.getTotalRevenue()))
            .build();
    }

    private TopProductDTO toTopProduct(ProductSalesRowDto row) {
        long qty = nullSafeLong(row.getQuantity());
        String name = productPerformanceRepository.findByProductId(row.getProductId())
            .map(ProductPerformance::getProductName)
            .orElse("Unknown product");
        return TopProductDTO.builder()
            .productId(row.getProductId())
            .name(name == null || name.isBlank() ? "Unknown product" : name)
            .quantity(qty)
            .sales(qty)
            .revenue(zeroIfNull(row.getRevenue()))
            .build();
    }

    private List<CategoryBreakdownDTO> aggregateCategories(List<CategorySalesReport> reports) {
        Map<String, CategoryBreakdownDTO> grouped = new LinkedHashMap<>();
        for (CategorySalesReport report : reports) {
            CategoryBreakdownDTO dto = grouped.computeIfAbsent(report.getCategoryName(), name -> CategoryBreakdownDTO.builder()
                .name(name)
                .revenue(BigDecimal.ZERO)
                .quantitySold(0L)
                .build());
            dto.setRevenue(zeroIfNull(dto.getRevenue()).add(zeroIfNull(report.getRevenue())));
            dto.setQuantitySold(nullSafeLong(dto.getQuantitySold()) + nullSafeLong(report.getQuantitySold()));
        }

        BigDecimal total = grouped.values().stream()
            .map(CategoryBreakdownDTO::getRevenue)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        grouped.values().forEach(dto -> {
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                dto.setValue(zeroIfNull(dto.getRevenue())
                    .divide(total, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue());
            } else {
                dto.setValue(0.0);
            }
        });

        return grouped.values().stream().toList();
    }

    private List<ChartPointDTO> buildAggregatedData(
        List<DailySalesReport> reports,
        StatsPeriodSupport.ResolvedPeriod resolved,
        PeriodWindow window
    ) {
        return switch (periodGranularity(resolved)) {
            case WEEK -> buildDailyBuckets(reports, window);
            case MONTH -> buildWeeklyBuckets(reports, window);
            case YEAR, ALL -> {
                if (periodGranularity(resolved) == StatsPeriod.ALL && reports.isEmpty()) {
                    yield List.of();
                }
                PeriodWindow chartWindow = window;
                if (periodGranularity(resolved) == StatsPeriod.ALL) {
                    LocalDate first = reports.get(0).getReportDate();
                    chartWindow = new PeriodWindow(first, window.to(), window.previousFrom(), window.previousTo());
                }
                yield buildMonthlyBuckets(reports, chartWindow);
            }
        };
    }

    private List<ChartPointDTO> buildVendorAggregatedData(
        List<VendorSalesReport> reports,
        StatsPeriodSupport.ResolvedPeriod resolved,
        PeriodWindow window
    ) {
        StatsPeriod grain = periodGranularity(resolved);
        PeriodWindow chartWindow = window;
        if (grain == StatsPeriod.ALL) {
            if (reports.isEmpty()) {
                return List.of();
            }
            LocalDate first = reports.get(0).getReportDate();
            chartWindow = new PeriodWindow(first, window.to(), window.previousFrom(), window.previousTo());
        }

        Map<String, ChartPointDTO> buckets = emptyChartBuckets(grain, chartWindow);
        for (VendorSalesReport report : reports) {
            String key = switch (grain) {
                case WEEK -> report.getReportDate().toString();
                case MONTH -> startOfWeek(report.getReportDate()).toString();
                case YEAR, ALL -> YearMonth.from(report.getReportDate()).atDay(1).toString();
            };
            ChartPointDTO point = buckets.get(key);
            if (point == null) {
                continue;
            }
            point.setRevenue(zeroIfNull(point.getRevenue()).add(zeroIfNull(report.getTotalRevenue())));
            point.setOrders(nullSafeLong(point.getOrders()) + nullSafeLong(report.getTotalOrders()));
        }
        return new ArrayList<>(buckets.values());
    }

    private Map<String, ChartPointDTO> emptyChartBuckets(StatsPeriod grain, PeriodWindow window) {
        Map<String, ChartPointDTO> buckets = new LinkedHashMap<>();
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd/MM");
        switch (grain) {
            case WEEK -> {
                for (LocalDate day = window.from(); !day.isAfter(window.to()); day = day.plusDays(1)) {
                    buckets.put(day.toString(), emptyPoint(day.toString(), day.format(label)));
                }
            }
            case MONTH -> {
                LocalDate weekStart = startOfWeek(window.from());
                for (LocalDate cursor = weekStart; !cursor.isAfter(window.to()); cursor = cursor.plusWeeks(1)) {
                    buckets.put(cursor.toString(), emptyPoint(cursor.toString(), "Tuần " + cursor.format(label)));
                }
            }
            case YEAR, ALL -> {
                YearMonth start = YearMonth.from(window.from());
                YearMonth end = YearMonth.from(window.to());
                for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
                    LocalDate monthStart = month.atDay(1);
                    buckets.put(monthStart.toString(), emptyPoint(
                        monthStart.toString(),
                        "Tháng " + month.getMonthValue() + "/" + month.getYear()
                    ));
                }
            }
        }
        return buckets;
    }

    private StatsPeriod periodGranularity(StatsPeriodSupport.ResolvedPeriod resolved) {
        long days = ChronoUnit.DAYS.between(resolved.from(), resolved.to()) + 1;
        if (days <= 7) {
            return StatsPeriod.WEEK;
        }
        if (days <= 31) {
            return StatsPeriod.MONTH;
        }
        if (days <= 366) {
            return StatsPeriod.YEAR;
        }
        return StatsPeriod.ALL;
    }

    private List<ChartPointDTO> buildFilledChartPoints(List<DailySalesReport> reports, PeriodWindow window) {
        long days = ChronoUnit.DAYS.between(window.from(), window.to()) + 1;
        if (days > 366) {
            if (reports.isEmpty()) {
                return List.of();
            }
            LocalDate first = reports.get(0).getReportDate();
            PeriodWindow trimmed = new PeriodWindow(first, window.to(), window.previousFrom(), window.previousTo());
            return buildMonthlyBuckets(reports, trimmed);
        }
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        for (LocalDate day = window.from(); !day.isAfter(window.to()); day = day.plusDays(1)) {
            buckets.put(day, emptyPoint(day.toString(), day.toString()));
        }
        for (DailySalesReport report : reports) {
            addDailyReport(buckets.get(report.getReportDate()), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildDailyBuckets(List<DailySalesReport> reports, PeriodWindow window) {
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        for (LocalDate day = window.from(); !day.isAfter(window.to()); day = day.plusDays(1)) {
            buckets.put(day, emptyPoint(day.toString(), day.format(label)));
        }
        for (DailySalesReport report : reports) {
            addDailyReport(buckets.get(report.getReportDate()), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildWeeklyBuckets(List<DailySalesReport> reports, PeriodWindow window) {
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        LocalDate weekStart = startOfWeek(window.from());
        for (LocalDate cursor = weekStart; !cursor.isAfter(window.to()); cursor = cursor.plusWeeks(1)) {
            buckets.put(cursor, emptyPoint(cursor.toString(), "Tuần " + cursor.format(label)));
        }
        for (DailySalesReport report : reports) {
            addDailyReport(buckets.get(startOfWeek(report.getReportDate())), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildMonthlyBuckets(List<DailySalesReport> reports, PeriodWindow window) {
        Map<YearMonth, ChartPointDTO> buckets = new LinkedHashMap<>();
        YearMonth start = YearMonth.from(window.from());
        YearMonth end = YearMonth.from(window.to());
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            LocalDate monthStart = month.atDay(1);
            buckets.put(month, emptyPoint(
                monthStart.toString(),
                "Tháng " + month.getMonthValue() + "/" + month.getYear()
            ));
        }
        for (DailySalesReport report : reports) {
            addDailyReport(buckets.get(YearMonth.from(report.getReportDate())), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private LocalDate startOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    private ChartPointDTO emptyPoint(String date, String name) {
        return ChartPointDTO.builder()
            .date(date)
            .name(name)
            .revenue(BigDecimal.ZERO)
            .orders(0L)
            .build();
    }

    private void addDailyReport(ChartPointDTO point, DailySalesReport report) {
        if (point == null) {
            return;
        }
        point.setRevenue(zeroIfNull(point.getRevenue()).add(zeroIfNull(report.getTotalRevenue())));
        point.setOrders(nullSafeLong(point.getOrders()) + nullSafeLong(report.getTotalOrders()));
    }

    private boolean isComparable(PeriodWindow window) {
        return !window.from().equals(window.previousFrom()) || !window.to().equals(window.previousTo());
    }
}
