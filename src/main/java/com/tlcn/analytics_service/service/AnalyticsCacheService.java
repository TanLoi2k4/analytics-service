package com.tlcn.analytics_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AnalyticsCacheService {

    public static final String ADMIN_DASHBOARD_KEY = "analytics:admin:dashboard";
    public static final String GROWTH_PLATFORM_REVENUE_KEY = "analytics:growth:platform:revenue";
    public static final String GROWTH_PLATFORM_ORDERS_KEY = "analytics:growth:platform:orders";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${analytics.cache.dashboard-ttl-minutes:10}")
    private long dashboardTtlMinutes;

    @Value("${analytics.cache.sales-ttl-minutes:10}")
    private long salesTtlMinutes;

    public AnalyticsCacheService(
        RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper
    ) {
        super();
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    public static String adminDashboardKey(String periodSuffix) {
        return ADMIN_DASHBOARD_KEY + ":" + (periodSuffix == null || periodSuffix.isBlank() ? "MONTH" : periodSuffix);
    }

    public static String vendorDashboardKey(String vendorKeycloakId) {
        return "analytics:vendor:" + vendorKeycloakId + ":dashboard";
    }

    public static String vendorDashboardKey(String vendorKeycloakId, String periodSuffix) {
        return vendorDashboardKey(vendorKeycloakId) + ":" + periodSuffix;
    }

    public static String vendorSalesKey(String vendorKeycloakId, String rangeCode) {
        return "analytics:vendor:" + vendorKeycloakId + ":sales:" + rangeCode;
    }

    public static String vendorRevenueGrowthKey(String vendorKeycloakId) {
        return "analytics:growth:vendor:" + vendorKeycloakId + ":revenue";
    }

    public static String vendorOrdersGrowthKey(String vendorKeycloakId) {
        return "analytics:growth:vendor:" + vendorKeycloakId + ":orders";
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception ex) {
            log.warn("Failed to read analytics cache key={}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Double> getGrowth(String key) {
        return get(key, Double.class);
    }

    public void putDashboard(String key, Object value) {
        put(key, value, Duration.ofMinutes(dashboardTtlMinutes));
    }

    public void putSales(String key, Object value) {
        put(key, value, Duration.ofMinutes(salesTtlMinutes));
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to write analytics cache key={}: {}", key, ex.getMessage());
        }
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void invalidateAfterOrderCompleted(Collection<String> vendorKeycloakIds) {
        evict(ADMIN_DASHBOARD_KEY);
        evictByPattern(ADMIN_DASHBOARD_KEY + "*");
        evict(GROWTH_PLATFORM_REVENUE_KEY);
        evict(GROWTH_PLATFORM_ORDERS_KEY);

        for (String vendorId : vendorKeycloakIds) {
            evictByPattern(vendorDashboardKey(vendorId) + "*");
            evictByPattern("analytics:vendor:" + vendorId + ":sales:*");
            evict(vendorRevenueGrowthKey(vendorId));
            evict(vendorOrdersGrowthKey(vendorId));
        }
    }
}
