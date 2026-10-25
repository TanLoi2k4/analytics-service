package com.tlcn.analytics_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.analytics_service.dto.kafka.OrderCompletedEvent;
import com.tlcn.analytics_service.service.AnalyticsAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderCompletedConsumer {

    private static final String IDEMPOTENCY_KEY_PREFIX = "kafka:order-completed:idempotency:";

    private final AnalyticsAggregationService analyticsAggregationService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${kafka.idempotency.ttl-hours:24}")
    private long idempotencyTtlHours;

    public OrderCompletedConsumer(
        AnalyticsAggregationService analyticsAggregationService,
        ObjectMapper objectMapper,
        RedisTemplate<String, String> redisTemplate
    ) {
        super();
        this.analyticsAggregationService = analyticsAggregationService;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }
    @KafkaListener(
        topics = "${spring.kafka.topics.order-completed:order-completed-topic}",
        groupId = "${spring.kafka.consumer.group-id:analytics-group}"
    )
    public void onOrderCompleted(ConsumerRecord<String, Object> record, Acknowledgment ack) {
        OrderCompletedEvent event = objectMapper.convertValue(record.value(), OrderCompletedEvent.class);
        String idempotencyKey = buildIdempotencyKey(event);

        if (idempotencyKey != null && !claim(idempotencyKey)) {
            log.info("Skip duplicate order-completed: eventId={} orderCode={} partition={} offset={}",
                event == null ? null : event.getEventId(),
                event == null ? null : event.getOrderCode(),
                record.partition(),
                record.offset()
            );
            acknowledge(ack);
            return;
        }

        try {
            analyticsAggregationService.processOrderCompleted(event);
            markProcessed(idempotencyKey);
            acknowledge(ack);
            log.debug("Acknowledged order-completed event: partition={}, offset={}", record.partition(), record.offset());
        } catch (Exception e) {
            release(idempotencyKey);
            log.error("Failed to process order-completed: eventId={} orderCode={} partition={} offset={} error={}",
                event == null ? null : event.getEventId(),
                event == null ? null : event.getOrderCode(),
                record.partition(),
                record.offset(),
                e.getMessage(),
                e
            );
            throw e;
        }
    }

    private String buildIdempotencyKey(OrderCompletedEvent event) {
        if (event == null) {
            return null;
        }
        if (event.getEventId() != null && !event.getEventId().isBlank()) {
            return IDEMPOTENCY_KEY_PREFIX + event.getEventId().trim();
        }
        if (event.getOrderCode() != null && !event.getOrderCode().isBlank()) {
            return IDEMPOTENCY_KEY_PREFIX + "order-completed-" + event.getOrderCode().trim();
        }
        return null;
    }

    private boolean claim(String key) {
        Boolean claimed = redisTemplate.opsForValue().setIfAbsent(
            key,
            "processing",
            Math.max(1L, idempotencyTtlHours),
            TimeUnit.HOURS
        );
        return Boolean.TRUE.equals(claimed);
    }

    private void markProcessed(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(
            key,
            "processed",
            Math.max(1L, idempotencyTtlHours),
            TimeUnit.HOURS
        );
    }

    private void release(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        redisTemplate.delete(key);
    }

    private void acknowledge(Acknowledgment ack) {
        if (ack != null) {
            ack.acknowledge();
        }
    }
}
