package com.tlcn.analytics_service.service;

import com.tlcn.analytics_service.enums.CustomerSegment;
import com.tlcn.analytics_service.model.CustomerVendorStats;
import com.tlcn.analytics_service.repository.CustomerVendorStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class CustomerSegmentationService {

    private final CustomerVendorStatsRepository customerVendorStatsRepository;

    @Value("${analytics.segmentation.vip-threshold:5000000}")
    private BigDecimal vipThreshold;

    @Value("${analytics.segmentation.new-customer-days:30}")
    private int newCustomerDays;

    public CustomerSegmentationService(CustomerVendorStatsRepository customerVendorStatsRepository) {
        super();
        this.customerVendorStatsRepository = customerVendorStatsRepository;
    }
    @Transactional
    public int refreshAllSegments() {
        List<CustomerVendorStats> customers = customerVendorStatsRepository.findAll();
        Instant newCustomerCutoff = Instant.now().minus(newCustomerDays, ChronoUnit.DAYS);

        for (CustomerVendorStats customer : customers) {
            customer.setSegment(resolveSegment(customer, newCustomerCutoff));
        }

        customerVendorStatsRepository.saveAll(customers);
        log.info("Refreshed customer segments for {} records", customers.size());
        return customers.size();
    }

    public CustomerSegment resolveSegment(CustomerVendorStats customer, Instant newCustomerCutoff) {
        BigDecimal spent = customer.getTotalSpent() == null ? BigDecimal.ZERO : customer.getTotalSpent();
        long orders = customer.getTotalOrders() == null ? 0L : customer.getTotalOrders();

        if (spent.compareTo(vipThreshold) >= 0) {
            return CustomerSegment.VIP;
        }
        if (orders >= 2) {
            return CustomerSegment.RETURNING;
        }
        if (orders == 1
            && customer.getFirstOrderAt() != null
            && customer.getFirstOrderAt().isAfter(newCustomerCutoff)) {
            return CustomerSegment.NEW;
        }
        return CustomerSegment.INACTIVE;
    }
}
