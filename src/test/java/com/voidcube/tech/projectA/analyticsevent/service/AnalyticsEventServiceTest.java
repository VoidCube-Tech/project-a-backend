package com.voidcube.tech.projectA.analyticsevent.service;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Async;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {

    @Mock
    private AnalyticsEventRepository analyticsEventRepository;

    @InjectMocks
    private AnalyticsEventService analyticsEventService;

    @Test
    void shouldSaveAnalyticsEvent() {
        AnalyticsEventRequestDTO request =
                new AnalyticsEventRequestDTO(1L, 15L, EventType.VIEW);

        analyticsEventService.saveAsync(request);

        AnalyticsEvent savedEvent = captureSavedEvent();

        assertThat(savedEvent.getLandingPageId()).isEqualTo(1L);
        assertThat(savedEvent.getProductId()).isEqualTo(15L);
        assertThat(savedEvent.getEventType()).isEqualTo(EventType.VIEW);
    }

    @Test
    void shouldSaveWhatsappClickWithoutProduct() {
        analyticsEventService.saveWhatsappClickAsync(1L, null);

        AnalyticsEvent savedEvent = captureSavedEvent();

        assertThat(savedEvent.getLandingPageId()).isEqualTo(1L);
        assertThat(savedEvent.getProductId()).isNull();
        assertThat(savedEvent.getEventType())
                .isEqualTo(EventType.WHATSAPP_CLICK);
    }

    @Test
    void saveMethodsShouldBeAsync() throws NoSuchMethodException {
        Method saveMethod = AnalyticsEventService.class.getMethod(
                "saveAsync",
                AnalyticsEventRequestDTO.class
        );

        Method whatsappMethod = AnalyticsEventService.class.getMethod(
                "saveWhatsappClickAsync",
                Long.class,
                Long.class
        );

        assertThat(saveMethod.isAnnotationPresent(Async.class)).isTrue();
        assertThat(whatsappMethod.isAnnotationPresent(Async.class)).isTrue();
    }

    private AnalyticsEvent captureSavedEvent() {
        ArgumentCaptor<AnalyticsEvent> captor =
                ArgumentCaptor.forClass(AnalyticsEvent.class);

        verify(analyticsEventRepository).save(captor.capture());

        return captor.getValue();
    }
}