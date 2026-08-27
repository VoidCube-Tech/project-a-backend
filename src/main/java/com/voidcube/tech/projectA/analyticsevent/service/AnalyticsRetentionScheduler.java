package com.voidcube.tech.projectA.analyticsevent.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.analytics.retention", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsRetentionScheduler {
    
    private final AnalyticsRetentionService analyticsRetentionService;

    @Scheduled(cron = "${app.analytics.retention.cron:0 0 3 * * *}", zone = "${app.analytics.retention.zone:UTC}")
    public void cleanupExpiredEvents() {
        int deleted = analyticsRetentionService.deleteExpiredEvents();

        if(deleted > 0) {
            log.info("Analytics retention removed " + "{} expired events", deleted);
        }
    }
}
