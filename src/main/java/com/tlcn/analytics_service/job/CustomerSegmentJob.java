package com.tlcn.analytics_service.job;

import com.tlcn.analytics_service.service.CustomerSegmentationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomerSegmentJob {

    private final CustomerSegmentationService customerSegmentationService;

    public CustomerSegmentJob(CustomerSegmentationService customerSegmentationService) {
        super();
        this.customerSegmentationService = customerSegmentationService;
    }
    @Scheduled(cron = "${analytics.jobs.customer-segment-cron:0 0 2 * * ?}")
    public void refreshSegments() {
        int updated = customerSegmentationService.refreshAllSegments();
        log.info("Customer segment job updated {} records", updated);
    }
}
