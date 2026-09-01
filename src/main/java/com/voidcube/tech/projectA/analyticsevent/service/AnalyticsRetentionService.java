package com.voidcube.tech.projectA.analyticsevent.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;

@Service
public class AnalyticsRetentionService {

    private final AnalyticsEventRepository analyticsEventRepository;
    private final Clock clock;
    private final int retentionMonths;
    private final int batchSize;

    public AnalyticsRetentionService(
            AnalyticsEventRepository analyticsEventRepository,
            Clock clock,
            @Value("${app.analytics.retention.months:12}") int retentionMonths,
            @Value("${app.analytics.retention.batch-size:1000}") int batchSize
    ) {
        if (retentionMonths <= 0) {
            throw new IllegalArgumentException("O período de retenção deve ser maior que zero.");
        }

        if (batchSize <= 0) {
            throw new IllegalArgumentException("O tamanho do lote deve ser maior que zero.");
        }

        this.analyticsEventRepository = analyticsEventRepository;
        this.clock = clock;
        this.retentionMonths = retentionMonths;
        this.batchSize = batchSize;
    }

    public int deleteExpiredEvents() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusMonths(retentionMonths);
        int totalDeleted = 0;
        int deletedInBatch;

        do {
            deletedInBatch = analyticsEventRepository.deleteBatchCreatedBefore(cutoff, batchSize);
            totalDeleted += deletedInBatch;
        } while (deletedInBatch == batchSize);

        return totalDeleted;
    }
}