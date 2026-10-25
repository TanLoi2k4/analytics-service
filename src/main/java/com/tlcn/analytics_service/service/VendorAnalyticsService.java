package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.client.OrderClient;
import com.tlcn.analytics_service.client.ProductClient;
import com.tlcn.analytics_service.dto.external.ProductSalesRowDto;
import com.tlcn.analytics_service.dto.*;
import com.tlcn.analytics_service.enums.CustomerSegment;
import com.tlcn.analytics_service.enums.StatsPeriod;
import com.tlcn.analytics_service.exception.ResourceNotFoundException;
import com.tlcn.analytics_service.model.CategorySalesReport;
import com.tlcn.analytics_service.model.CustomerVendorStats;
import com.tlcn.analytics_service.model.ProductPerformance;
import com.tlcn.analytics_service.model.VendorSalesReport;
import com.tlcn.analytics_service.repository.CategorySalesReportRepository;
import com.tlcn.analytics_service.repository.CustomerVendorStatsRepository;
import com.tlcn.analytics_service.repository.ProductPerformanceRepository;
import com.tlcn.analytics_service.repository.VendorSalesReportRepository;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport;
import com.tlcn.analytics_service.util.AnalyticsMetricsSupport.PeriodWindow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.tlcn.analytics_service.util.AnalyticsMetricsSupport.*;

@Service
public class VendorAnalyticsService {

    private final VendorSalesReportRepository vendorSalesReportRepository;
    private final ProductPerformanceRepository productPerformanceRepository;
    private final CustomerVendorStatsRepository customerVendorStatsRepository;
    private final CategorySalesReportRepository categorySalesReportRepository;
    private final AnalyticsCacheService analyticsCacheService;
    private final CustomerSegmentationService customerSegmentationService;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    public VendorAnalyticsService(
        VendorSalesReportRepository vendorSalesReportRepository,
        ProductPerformanceRepository productPerformanceRepository,
        CustomerVendorStatsRepository customerVendorStatsRepository,
        CategorySalesReportRepository categorySalesReportRepository,
        AnalyticsCacheService analyticsCacheService,
        CustomerSegmentationService customerSegmentationService,
        ProductClient productClient,
        OrderClient orderClient
    ) {
        super();
        this.vendorSalesReportRepository = vendorSalesReportRepository;
        this.productPerformanceRepository = productPerformanceRepository;
        this.customerVendorStatsRepository = customerVendorStatsRepository;
        this.categorySalesReportRepository = categorySalesReportRepository;
        this.analyticsCacheService = analyticsCacheService;
        this.customerSegmentationService = customerSegmentationService;
        this.productClient = productClient;
        this.orderClient = orderClient;
    }
    public VendorDashboardResponse getDashboard(
        String vendorKeycloakId,
        StatsPeriod period,
        LocalDate anchor
    ) {
        StatsPeriodSupport.ResolvedPeriod resolved = StatsPeriodSupport.resolve(period, anchor);
        String cacheKey = AnalyticsCacheService.vendorDashboardKey(
            vendorKeycloakId,
            StatsPeriodSupport.cacheSuffix(period, anchor)
        );
        return analyticsCacheService.get(cacheKey, VendorDashboardResponse.class)
            .orElseGet(() -> {
                VendorDashboardResponse response = buildDashboard(vendorKeycloakId, resolved.toWindow());
                analyticsCacheService.putDashboard(cacheKey, response);
                return response;
            });
    }

    public SalesAnalyticsResponse getSalesAnalytics(
        String vendorKeycloakId,
        StatsPeriod period,
        LocalDate anchor
    ) {
        StatsPeriodSupport.ResolvedPeriod resolved = StatsPeriodSupport.resolve(period, anchor);
        String cacheKey = AnalyticsCacheService.vendorSalesKey(
            vendorKeycloakId,
            StatsPeriodSupport.cacheSuffix(period, anchor)
        );
        return analyticsCacheService.get(cacheKey, SalesAnalyticsResponse.class)
            .orElseGet(() -> {
                SalesAnalyticsResponse response = buildSalesAnalytics(vendorKeycloakId, resolved);
                analyticsCacheService.putSales(cacheKey, response);
                return response;
            });
    }

    public List<TopProductDTO> getTopProducts(
        String vendorKeycloakId,
        int limit,
        boolean enrich,
        StatsPeriod period,
        LocalDate anchor
    ) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        int safeLimit = Math.max(limit, 1);

        List<ProductSalesRowDto> rows = orderClient.getVendorProductSales(
            vendorKeycloakId,
            window.from(),
            window.to(),
            safeLimit
        );

        List<TopProductDTO> products;
        if (rows == null) {
            products = productPerformanceRepository
                .findByVendorIdOrderByTotalRevenueDesc(vendorKeycloakId, PageRequest.of(0, safeLimit))
                .stream()
                .map(this::toTopProduct)
                .toList();
        } else {
            products = rows.stream()
                .filter(row -> row.getProductId() != null)
                .map(this::toTopProduct)
                .toList();
        }

        if (!enrich) {
            return products;
        }

        return products.stream()
            .map(productClient::enrich)
            .toList();
    }

    public List<CategoryBreakdownDTO> getProductsByCategory(
        String vendorKeycloakId,
        StatsPeriod period,
        LocalDate anchor
    ) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        return getProductsByCategoryForPeriod(vendorKeycloakId, window);
    }

    public CustomerGrowthResponse getCustomerGrowth(
        String vendorKeycloakId,
        StatsPeriod period,
        LocalDate anchor
    ) {
        PeriodWindow window = StatsPeriodSupport.resolve(period, anchor).toWindow();
        ZoneId zone = StatsPeriodSupport.ZONE;
        Instant fromInstant = window.from().atStartOfDay(zone).toInstant();
        Instant toInstant = window.to().plusDays(1).atStartOfDay(zone).toInstant();

        List<CustomerVendorStats> customers = customerVendorStatsRepository.findByVendorAndFirstOrderBetween(
            vendorKeycloakId, fromInstant, toInstant
        );

        Map<LocalDate, Long> dailyNew = new TreeMap<>();
        for (CustomerVendorStats customer : customers) {
            if (customer.getFirstOrderAt() == null) {
                continue;
            }
            LocalDate day = customer.getFirstOrderAt().atZone(zone).toLocalDate();
            dailyNew.merge(day, 1L, Long::sum);
        }

        long days = ChronoUnit.DAYS.between(window.from(), window.to()) + 1;
        long cumulative = 0L;
        List<ChartPointDTO> chartData = new ArrayList<>();
        if (days > 366) {
            Map<YearMonth, Long> monthlyNew = new TreeMap<>();
            dailyNew.forEach((day, count) -> monthlyNew.merge(YearMonth.from(day), count, Long::sum));
            YearMonth start = YearMonth.from(window.from());
            YearMonth end = YearMonth.from(window.to());
            for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
                long monthNew = monthlyNew.getOrDefault(month, 0L);
                cumulative += monthNew;
                LocalDate monthStart = month.atDay(1);
                chartData.add(ChartPointDTO.builder()
                    .date(monthStart.toString())
                    .name("Tháng " + month.getMonthValue() + "/" + month.getYear())
                    .value(cumulative)
                    .orders(monthNew)
                    .build());
            }
        } else {
            for (LocalDate day = window.from(); !day.isAfter(window.to()); day = day.plusDays(1)) {
                long dayNew = dailyNew.getOrDefault(day, 0L);
                cumulative += dayNew;
                chartData.add(ChartPointDTO.builder()
                    .date(day.toString())
                    .name(day.toString())
                    .value(cumulative)
                    .orders(dayNew)
                    .build());
            }
        }

        return CustomerGrowthResponse.builder().chartData(chartData).build();
    }

    public CustomerSegmentsResponse getCustomerSegments(String vendorKeycloakId) {
        List<CustomerVendorStats> customers = customerVendorStatsRepository.findByVendorKeycloakId(vendorKeycloakId);
        Instant newCustomerCutoff = Instant.now().minus(30, ChronoUnit.DAYS);

        long newCount = 0L;
        long regular = 0L;
        long vip = 0L;
        long inactive = 0L;

        for (CustomerVendorStats customer : customers) {
            CustomerSegment segment = customer.getSegment() != null
                ? customer.getSegment()
                : customerSegmentationService.resolveSegment(customer, newCustomerCutoff);
            switch (segment) {
                case NEW -> newCount++;
                case RETURNING -> regular++;
                case VIP -> vip++;
                case INACTIVE -> inactive++;
            }
        }

        return CustomerSegmentsResponse.builder()
            .newCustomers(newCount)
            .regular(regular)
            .vip(vip)
            .inactive(inactive)
            .build();
    }

    public List<TopCustomerDTO> getTopCustomers(String vendorKeycloakId, int limit) {
        return customerVendorStatsRepository
            .findByVendorKeycloakIdOrderByTotalSpentDesc(vendorKeycloakId, PageRequest.of(0, Math.max(limit, 1)))
            .stream()
            .map(this::toTopCustomer)
            .toList();
    }

    public PageResponse<VendorCustomerDTO> listCustomers(
        String vendorKeycloakId,
        int page,
        int size,
        String search,
        String sortBy,
        String sortOrder
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
            safePage,
            safeSize,
            Sort.by(direction, resolveCustomerSortField(sortBy))
        );

        String trimmedSearch = search == null ? null : search.trim();
        if (trimmedSearch != null && trimmedSearch.isEmpty()) {
            trimmedSearch = null;
        }

        Page<CustomerVendorStats> result = customerVendorStatsRepository.searchByVendor(
            vendorKeycloakId,
            trimmedSearch,
            pageable
        );

        return PageResponse.<VendorCustomerDTO>builder()
            .items(result.getContent().stream().map(this::toVendorCustomer).toList())
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();
    }

    public VendorCustomerDTO getCustomerDetail(String vendorKeycloakId, String customerKeycloakId) {
        return customerVendorStatsRepository
            .findByVendorKeycloakIdAndCustomerKeycloakId(vendorKeycloakId, customerKeycloakId)
            .map(this::toVendorCustomer)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public SummaryStatsDTO getCustomerSummary(String vendorKeycloakId) {
        long totalCustomers = customerVendorStatsRepository.countByVendorKeycloakId(vendorKeycloakId);
        List<CustomerVendorStats> customers = customerVendorStatsRepository.findByVendorKeycloakId(vendorKeycloakId);

        BigDecimal totalSpent = customers.stream()
            .map(CustomerVendorStats::getTotalSpent)
            .map(AnalyticsMetricsSupport::zeroIfNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = customers.stream()
            .mapToLong(customer -> nullSafeLong(customer.getTotalOrders()))
            .sum();

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant sixtyDaysAgo = Instant.now().minus(60, ChronoUnit.DAYS);
        long newCustomers = customers.stream()
            .filter(customer -> customer.getFirstOrderAt() != null && customer.getFirstOrderAt().isAfter(thirtyDaysAgo))
            .count();
        long previousNewCustomers = customerVendorStatsRepository.countNewCustomersByVendor(
            vendorKeycloakId, sixtyDaysAgo, thirtyDaysAgo
        );

        return SummaryStatsDTO.builder()
            .totalCustomers(totalCustomers)
            .newCustomers(newCustomers)
            .totalRevenue(totalSpent)
            .totalOrders(totalOrders)
            .avgOrderValue(avgOrderValue(totalSpent, totalOrders))
            .growth(calculateGrowth(newCustomers, previousNewCustomers))
            .build();
    }

    public RevenueReportResponse getRevenueReport(String vendorKeycloakId, LocalDate from, LocalDate to) {
        PeriodWindow period = resolveCustomPeriod(from, to);
        List<VendorSalesReport> current = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.from(), period.to()
        );
        List<VendorSalesReport> previous = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.previousFrom(), period.previousTo()
        );

        BigDecimal gross = sumVendorRevenue(current);
        BigDecimal previousGross = sumVendorRevenue(previous);
        BigDecimal commission = sumVendorCommission(current);
        long orders = sumVendorOrders(current);

        SummaryStatsDTO summary = SummaryStatsDTO.builder()
            .totalRevenue(gross)
            .commission(commission)
            .netRevenue(gross.subtract(commission).max(BigDecimal.ZERO))
            .totalOrders(orders)
            .avgOrderValue(avgOrderValue(gross, orders))
            .growth(calculateGrowth(gross, previousGross))
            .build();

        List<ChartPointDTO> chartData = current.stream()
            .map(report -> ChartPointDTO.builder()
                .date(report.getReportDate().toString())
                .name(report.getReportDate().toString())
                .revenue(zeroIfNull(report.getTotalRevenue()))
                .orders(nullSafeLong(report.getTotalOrders()))
                .build())
            .toList();

        List<CategoryBreakdownDTO> categoryData = getProductsByCategoryForPeriod(vendorKeycloakId, period);

        return RevenueReportResponse.builder()
            .summary(summary)
            .chartData(chartData)
            .categoryData(categoryData)
            .build();
    }

    private List<CategoryBreakdownDTO> getProductsByCategoryForPeriod(String vendorKeycloakId, PeriodWindow period) {
        List<CategorySalesReport> reports = categorySalesReportRepository.findByScopeKeyAndReportDateBetween(
            vendorKeycloakId,
            period.from(),
            period.to()
        );
        return aggregateCategories(reports);
    }

    private VendorDashboardResponse buildDashboard(String vendorKeycloakId, PeriodWindow period) {
        List<VendorSalesReport> current = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.from(), period.to()
        );
        List<VendorSalesReport> previous = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.previousFrom(), period.previousTo()
        );

        BigDecimal revenue = sumVendorRevenue(current);
        long orders = sumVendorOrders(current);
        BigDecimal previousRevenue = sumVendorRevenue(previous);
        long previousOrders = sumVendorOrders(previous);

        TopProductDTO topProduct = productPerformanceRepository
            .findByVendorIdOrderByTotalRevenueDesc(vendorKeycloakId, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .map(this::toTopProduct)
            .orElse(null);

        return VendorDashboardResponse.builder()
            .totalRevenue(revenue)
            .totalOrders(orders)
            .revenueGrowth(resolveVendorRevenueGrowth(vendorKeycloakId, revenue, previousRevenue))
            .ordersGrowth(resolveVendorOrdersGrowth(vendorKeycloakId, orders, previousOrders))
            .topProduct(topProduct)
            .avgOrderValue(avgOrderValue(revenue, orders))
            .build();
    }

    private double resolveVendorRevenueGrowth(String vendorKeycloakId, BigDecimal revenue, BigDecimal previousRevenue) {
        return analyticsCacheService.getGrowth(AnalyticsCacheService.vendorRevenueGrowthKey(vendorKeycloakId))
            .orElseGet(() -> calculateGrowth(revenue, previousRevenue));
    }

    private double resolveVendorOrdersGrowth(String vendorKeycloakId, long orders, long previousOrders) {
        return analyticsCacheService.getGrowth(AnalyticsCacheService.vendorOrdersGrowthKey(vendorKeycloakId))
            .orElseGet(() -> calculateGrowth(orders, previousOrders));
    }

    private SalesAnalyticsResponse buildSalesAnalytics(
        String vendorKeycloakId,
        StatsPeriodSupport.ResolvedPeriod resolved
    ) {
        PeriodWindow period = resolved.toWindow();
        List<VendorSalesReport> current = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.from(), period.to()
        );
        List<VendorSalesReport> previous = vendorSalesReportRepository.findByVendorIdAndReportDateBetweenOrderByReportDateAsc(
            vendorKeycloakId, period.previousFrom(), period.previousTo()
        );

        BigDecimal revenue = sumVendorRevenue(current);
        long orders = sumVendorOrders(current);
        BigDecimal previousRevenue = sumVendorRevenue(previous);
        long previousOrders = sumVendorOrders(previous);
        BigDecimal aov = avgOrderValue(revenue, orders);
        BigDecimal previousAov = avgOrderValue(previousRevenue, previousOrders);
        boolean comparable = !period.from().equals(period.previousFrom())
            || !period.to().equals(period.previousTo());

        SummaryStatsDTO summary = SummaryStatsDTO.builder()
            .totalRevenue(revenue)
            .totalOrders(orders)
            .avgOrderValue(aov)
            .growth(comparable ? growthOrNull(revenue, previousRevenue) : null)
            .ordersGrowth(comparable ? growthOrNull(orders, previousOrders) : null)
            .avgOrderValueGrowth(comparable ? growthOrNull(aov, previousAov) : null)
            .build();

        List<ChartPointDTO> chartData = buildFilledChartPoints(current, period);
        List<ChartPointDTO> weeklyData = buildAggregatedData(current, resolved, period);

        return SalesAnalyticsResponse.builder()
            .summary(summary)
            .chartData(chartData)
            .weeklyData(weeklyData)
            .build();
    }

    private List<ChartPointDTO> buildAggregatedData(
        List<VendorSalesReport> reports,
        StatsPeriodSupport.ResolvedPeriod resolved,
        PeriodWindow period
    ) {
        return switch (periodGranularity(resolved)) {
            case WEEK -> buildDailyBuckets(reports, period);
            case MONTH -> buildWeeklyBuckets(reports, period);
            case YEAR, ALL -> {
                PeriodWindow window = period;
                if (periodGranularity(resolved) == StatsPeriod.ALL && !reports.isEmpty()) {
                    LocalDate first = reports.get(0).getReportDate();
                    window = new PeriodWindow(first, period.to(), period.previousFrom(), period.previousTo());
                }
                yield buildMonthlyBuckets(reports, window);
            }
        };
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

    private List<ChartPointDTO> buildFilledChartPoints(List<VendorSalesReport> reports, PeriodWindow period) {
        long days = ChronoUnit.DAYS.between(period.from(), period.to()) + 1;
        if (days > 366) {
            if (reports.isEmpty()) {
                return List.of();
            }
            LocalDate first = reports.get(0).getReportDate();
            PeriodWindow trimmed = new PeriodWindow(first, period.to(), period.previousFrom(), period.previousTo());
            return buildMonthlyBuckets(reports, trimmed);
        }
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        LocalDate end = period.to();
        for (LocalDate day = period.from(); !day.isAfter(end); day = day.plusDays(1)) {
            buckets.put(day, emptyPoint(day.toString(), day.toString()));
        }
        for (VendorSalesReport report : reports) {
            addReport(buckets.get(report.getReportDate()), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildDailyBuckets(List<VendorSalesReport> reports, PeriodWindow period) {
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        for (LocalDate day = period.from(); !day.isAfter(period.to()); day = day.plusDays(1)) {
            buckets.put(day, emptyPoint(day.toString(), day.format(label)));
        }
        for (VendorSalesReport report : reports) {
            addReport(buckets.get(report.getReportDate()), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildWeeklyBuckets(List<VendorSalesReport> reports, PeriodWindow period) {
        DateTimeFormatter label = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, ChartPointDTO> buckets = new LinkedHashMap<>();
        LocalDate weekStart = startOfWeek(period.from());
        for (LocalDate cursor = weekStart; !cursor.isAfter(period.to()); cursor = cursor.plusWeeks(1)) {
            buckets.put(cursor, emptyPoint(cursor.toString(), "Tuần " + cursor.format(label)));
        }
        for (VendorSalesReport report : reports) {
            addReport(buckets.get(startOfWeek(report.getReportDate())), report);
        }
        return new ArrayList<>(buckets.values());
    }

    private List<ChartPointDTO> buildMonthlyBuckets(List<VendorSalesReport> reports, PeriodWindow period) {
        Map<YearMonth, ChartPointDTO> buckets = new LinkedHashMap<>();
        YearMonth start = YearMonth.from(period.from());
        YearMonth end = YearMonth.from(period.to());
        for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
            LocalDate monthStart = month.atDay(1);
            buckets.put(month, emptyPoint(
                monthStart.toString(),
                "Tháng " + month.getMonthValue() + "/" + month.getYear()
            ));
        }
        for (VendorSalesReport report : reports) {
            addReport(buckets.get(YearMonth.from(report.getReportDate())), report);
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

    private void addReport(ChartPointDTO point, VendorSalesReport report) {
        if (point == null) {
            return;
        }
        point.setRevenue(zeroIfNull(point.getRevenue()).add(zeroIfNull(report.getTotalRevenue())));
        point.setOrders(nullSafeLong(point.getOrders()) + nullSafeLong(report.getTotalOrders()));
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
                double pct = zeroIfNull(dto.getRevenue())
                    .divide(total, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
                dto.setValue(pct);
            } else {
                dto.setValue(0.0);
            }
        });

        return grouped.values().stream().toList();
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

    private TopCustomerDTO toTopCustomer(CustomerVendorStats stats) {
        VendorCustomerDTO customer = toVendorCustomer(stats);
        return TopCustomerDTO.builder()
            .customerKeycloakId(customer.getCustomerKeycloakId())
            .name(customer.getName())
            .phone(customer.getPhone())
            .orders(customer.getOrders())
            .spent(customer.getSpent())
            .build();
    }

    private VendorCustomerDTO toVendorCustomer(CustomerVendorStats stats) {
        String name = stats.getCustomerName();
        if (name == null || name.isBlank()) {
            name = maskCustomerId(stats.getCustomerKeycloakId());
        }

        Instant newCustomerCutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        CustomerSegment segment = stats.getSegment() != null
            ? stats.getSegment()
            : customerSegmentationService.resolveSegment(stats, newCustomerCutoff);

        return VendorCustomerDTO.builder()
            .customerKeycloakId(stats.getCustomerKeycloakId())
            .name(name)
            .phone(stats.getCustomerPhone())
            .orders(nullSafeLong(stats.getTotalOrders()))
            .spent(zeroIfNull(stats.getTotalSpent()))
            .firstOrderAt(stats.getFirstOrderAt())
            .lastOrderAt(stats.getLastOrderAt())
            .segment(segment)
            .build();
    }

    private String resolveCustomerSortField(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "lastOrderAt";
        }
        return switch (sortBy.toLowerCase()) {
            case "name" -> "customerName";
            case "totalorders", "orders" -> "totalOrders";
            case "totalspent", "spent" -> "totalSpent";
            case "firstorderat" -> "firstOrderAt";
            case "lastorderat" -> "lastOrderAt";
            default -> "lastOrderAt";
        };
    }

    private String maskCustomerId(String customerKeycloakId) {
        if (customerKeycloakId == null || customerKeycloakId.length() < 8) {
            return "Customer";
        }
        return "Customer " + customerKeycloakId.substring(0, 8);
    }
}
