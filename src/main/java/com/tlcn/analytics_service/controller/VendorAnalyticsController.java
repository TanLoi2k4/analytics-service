package com.tlcn.analytics_service.controller;

import com.tlcn.analytics_service.dto.*;
import com.tlcn.analytics_service.enums.StatsPeriod;
import com.tlcn.analytics_service.service.VendorAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics/vendor")
@PreAuthorize("hasRole('VENDOR')")
public class VendorAnalyticsController {

    private final VendorAnalyticsService vendorAnalyticsService;

    public VendorAnalyticsController(VendorAnalyticsService vendorAnalyticsService) {
        super();
        this.vendorAnalyticsService = vendorAnalyticsService;
    }
    @GetMapping("/dashboard")
    public ResponseEntity<ResponseDTO<VendorDashboardResponse>> getDashboard(
        Authentication authentication,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        VendorDashboardResponse data = vendorAnalyticsService.getDashboard(
            authentication.getName(),
            period,
            anchor
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/sales")
    public ResponseEntity<ResponseDTO<SalesAnalyticsResponse>> getSales(
        Authentication authentication,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        SalesAnalyticsResponse data = vendorAnalyticsService.getSalesAnalytics(
            authentication.getName(),
            period,
            anchor
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/products/top")
    public ResponseEntity<ResponseDTO<List<TopProductDTO>>> getTopProducts(
        Authentication authentication,
        @RequestParam(name = "limit", defaultValue = "10") int limit,
        @RequestParam(name = "enrich", defaultValue = "false") boolean enrich,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        List<TopProductDTO> data = vendorAnalyticsService.getTopProducts(
            authentication.getName(),
            limit,
            enrich,
            period,
            anchor
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/products/by-category")
    public ResponseEntity<ResponseDTO<List<CategoryBreakdownDTO>>> getProductsByCategory(
        Authentication authentication,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        List<CategoryBreakdownDTO> data = vendorAnalyticsService.getProductsByCategory(
            authentication.getName(),
            period,
            anchor
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers/top")
    public ResponseEntity<ResponseDTO<List<TopCustomerDTO>>> getTopCustomers(
        Authentication authentication,
        @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        List<TopCustomerDTO> data = vendorAnalyticsService.getTopCustomers(authentication.getName(), limit);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers")
    public ResponseEntity<ResponseDTO<PageResponse<VendorCustomerDTO>>> listCustomers(
        Authentication authentication,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestParam(name = "search", required = false) String search,
        @RequestParam(name = "sortBy", required = false) String sortBy,
        @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder
    ) {
        PageResponse<VendorCustomerDTO> data = vendorAnalyticsService.listCustomers(
            authentication.getName(),
            page,
            size,
            search,
            sortBy,
            sortOrder
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers/{customerKeycloakId}")
    public ResponseEntity<ResponseDTO<VendorCustomerDTO>> getCustomerDetail(
        Authentication authentication,
        @PathVariable("customerKeycloakId") String customerKeycloakId
    ) {
        VendorCustomerDTO data = vendorAnalyticsService.getCustomerDetail(authentication.getName(), customerKeycloakId);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers/summary")
    public ResponseEntity<ResponseDTO<SummaryStatsDTO>> getCustomerSummary(Authentication authentication) {
        SummaryStatsDTO data = vendorAnalyticsService.getCustomerSummary(authentication.getName());
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers/growth")
    public ResponseEntity<ResponseDTO<CustomerGrowthResponse>> getCustomerGrowth(
        Authentication authentication,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        CustomerGrowthResponse data = vendorAnalyticsService.getCustomerGrowth(
            authentication.getName(),
            period,
            anchor
        );
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/customers/segments")
    public ResponseEntity<ResponseDTO<CustomerSegmentsResponse>> getCustomerSegments(Authentication authentication) {
        CustomerSegmentsResponse data = vendorAnalyticsService.getCustomerSegments(authentication.getName());
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/revenue-report")
    public ResponseEntity<ResponseDTO<RevenueReportResponse>> getRevenueReport(
        Authentication authentication,
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        RevenueReportResponse data = vendorAnalyticsService.getRevenueReport(authentication.getName(), from, to);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }
}
