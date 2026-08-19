package com.voidcube.tech.projectA.analyticsevent.controller;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.service.AnalyticsEventService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AnalyticsEventControllerTest {

    @Test
    void shouldAcceptEventForAsyncProcessing() {
        AnalyticsEventService service =
                mock(AnalyticsEventService.class);

        AnalyticsEventController controller =
                new AnalyticsEventController(service);

        AnalyticsEventRequestDTO request =
                new AnalyticsEventRequestDTO(
                        1L,
                        15L,
                        EventType.ADD_TO_CART
                );

        ResponseEntity<Void> response =
                controller.register(request);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);

        verify(service).saveAsync(request);
    }
}
