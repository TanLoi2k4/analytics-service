package com.tlcn.analytics_service.controller;

import com.tlcn.analytics_service.dto.*;
import com.tlcn.analytics_service.enums.StatsPeriod;
import com.tlcn.analytics_service.service.AdminAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        super();
        this.adminAnalyticsService = adminAnalyticsService;
    }
    @GetMapping("/dashboard")
    public ResponseEntity<ResponseDTO<AdminDashboardResponse>> getDashboard(
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        AdminDashboardResponse data = adminAnalyticsService.getDashboard(period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/revenue")
    public ResponseEntity<ResponseDTO<SalesAnalyticsResponse>> getRevenue(
        @RequestParam(name = "range", required = false) String range,
        @RequestParam(name = "period", required = false) StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        SalesAnalyticsResponse data = period != null
            ? adminAnalyticsService.getRevenueChart(period, anchor)
            : adminAnalyticsService.getRevenueChart(range == null || range.isBlank() ? "30d" : range);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/revenue-report")
    public ResponseEntity<ResponseDTO<RevenueReportResponse>> getRevenueReport(
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        RevenueReportResponse data = adminAnalyticsService.getRevenueReport(period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/vendors/top")
    public ResponseEntity<ResponseDTO<List<TopVendorDTO>>> getTopVendors(
        @RequestParam(name = "limit", defaultValue = "10") int limit,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        List<TopVendorDTO> data = adminAnalyticsService.getTopVendors(limit, period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/vendors/{vendorId}")
    public ResponseEntity<ResponseDTO<VendorDetailAnalyticsResponse>> getVendorDetail(
        @PathVariable("vendorId") String vendorId,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        VendorDetailAnalyticsResponse data = adminAnalyticsService.getVendorDetail(vendorId, period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/products/top")
    public ResponseEntity<ResponseDTO<List<TopProductDTO>>> getTopProducts(
        @RequestParam(name = "limit", defaultValue = "20") int limit,
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        List<TopProductDTO> data = adminAnalyticsService.getTopProducts(limit, period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }

    @GetMapping("/products/by-category")
    public ResponseEntity<ResponseDTO<List<CategoryBreakdownDTO>>> getProductsByCategory(
        @RequestParam(name = "period", defaultValue = "MONTH") StatsPeriod period,
        @RequestParam(name = "anchor", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate anchor
    ) {
        List<CategoryBreakdownDTO> data = adminAnalyticsService.getProductsByCategory(period, anchor);
        return ResponseEntity.ok(new ResponseDTO<>(true, "OK", data));
    }
}
