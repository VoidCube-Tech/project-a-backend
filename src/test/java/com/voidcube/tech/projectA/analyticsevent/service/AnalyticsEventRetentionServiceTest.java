package com.voidcube.tech.projectA.analyticsevent.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsRetentionServiceTest {

    @Mock
    private AnalyticsEventRepository
            analyticsEventRepository;

    @Test
    void shouldDeleteExpiredEventsInBatches() {
        Clock clock = Clock.fixed(
                Instant.parse(
                        "2026-08-27T03:00:00Z"
                ),
                ZoneOffset.UTC
        );

        AnalyticsRetentionService service =
                new AnalyticsRetentionService(
                        analyticsEventRepository,
                        clock,
                        12,
                        1000
                );

        LocalDateTime cutoff =
                LocalDateTime.of(
                        2025,
                        8,
                        27,
                        3,
                        0
                );

        when(
                analyticsEventRepository
                        .deleteBatchCreatedBefore(
                                cutoff,
                                1000
                        )
        ).thenReturn(
                1000,
                1000,
                250
        );

        int totalDeleted =
                service.deleteExpiredEvents();

        assertThat(totalDeleted)
                .isEqualTo(2250);

        verify(
                analyticsEventRepository,
                times(3)
        ).deleteBatchCreatedBefore(
                cutoff,
                1000
        );
    }

    @Test
    void shouldStopAfterFirstIncompleteBatch() {
        Clock clock = Clock.fixed(
                Instant.parse(
                        "2026-08-27T03:00:00Z"
                ),
                ZoneOffset.UTC
        );

        AnalyticsRetentionService service =
                new AnalyticsRetentionService(
                        analyticsEventRepository,
                        clock,
                        12,
                        1000
                );

        LocalDateTime cutoff =
                LocalDateTime.of(
                        2025,
                        8,
                        27,
                        3,
                        0
                );

        when(
                analyticsEventRepository
                        .deleteBatchCreatedBefore(
                                cutoff,
                                1000
                        )
        ).thenReturn(0);

        assertThat(service.deleteExpiredEvents())
                .isZero();

        verify(analyticsEventRepository)
                .deleteBatchCreatedBefore(
                        cutoff,
                        1000
                );
    }
}
