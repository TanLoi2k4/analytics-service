package com.tlcn.analytics_service.job;

import com.tlcn.analytics_service.service.GrowthCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GrowthCalculationJob {

    private final GrowthCalculationService growthCalculationService;

    public GrowthCalculationJob(GrowthCalculationService growthCalculationService) {
        super();
        this.growthCalculationService = growthCalculationService;
    }
    @Scheduled(cron = "${analytics.jobs.growth-calculation-cron:0 30 1 * * ?}")
    public void calculateGrowth() {
        growthCalculationService.refreshPlatformGrowthCache();
        log.info("Growth calculation job completed");
    }
}
