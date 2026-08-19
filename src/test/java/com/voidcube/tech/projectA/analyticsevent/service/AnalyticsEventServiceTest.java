package com.voidcube.tech.projectA.analyticsevent.service;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {

    @Mock
    private AnalyticsEventRepository
            analyticsEventRepository;

    @InjectMocks
    private AnalyticsEventService
            analyticsEventService;

    @Test
    void shouldSaveAnalyticsEvent() {
        AnalyticsEventRequestDTO request =
                new AnalyticsEventRequestDTO(
                        1L,
                        15L,
                        EventType.VIEW
                );

        analyticsEventService.saveAsync(request);

        ArgumentCaptor<AnalyticsEvent> captor =
                ArgumentCaptor.forClass(
                        AnalyticsEvent.class
                );

        verify(analyticsEventRepository)
                .save(captor.capture());

        AnalyticsEvent savedEvent =
                captor.getValue();

        assertThat(savedEvent.getLandingPageId())
                .isEqualTo(1L);

        assertThat(savedEvent.getProductId())
                .isEqualTo(15L);

        assertThat(savedEvent.getEventType())
                .isEqualTo(EventType.VIEW);
    }

    @Test
    void saveMethodShouldBeAsync()
            throws NoSuchMethodException {

        Method method =
                AnalyticsEventService.class
                        .getMethod(
                                "saveAsync",
                                AnalyticsEventRequestDTO.class
                        );

        assertThat(
                method.isAnnotationPresent(
                        Async.class
                )
        ).isTrue();
    }
}
