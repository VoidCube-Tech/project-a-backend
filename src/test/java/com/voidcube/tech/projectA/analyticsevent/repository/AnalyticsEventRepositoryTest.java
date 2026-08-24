package com.voidcube.tech.projectA.analyticsevent.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.projection.AnalyticsEventCountProjection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest(
    properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
class AnalyticsEventRepositoryTest {

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Test
    void deveAgruparEContarEventosPorProdutoETipo() {
        analyticsEventRepository.saveAllAndFlush(
                List.of(
                        createEvent(
                                1L,
                                10L,
                                EventType.VIEW
                        ),
                        createEvent(
                                1L,
                                10L,
                                EventType.VIEW
                        ),
                        createEvent(
                                1L,
                                20L,
                                EventType.ADD_TO_CART
                        )
                )
        );

        List<AnalyticsEventCountProjection> result =
                analyticsEventRepository
                        .countEventsByProductIds(
                                List.of(10L, 20L)
                        );

        assertThat(result)
                .extracting(
                        AnalyticsEventCountProjection::getProductId,
                        AnalyticsEventCountProjection::getEventType,
                        AnalyticsEventCountProjection::getEventCount
                )
                .containsExactly(
                        tuple(
                                10L,
                                EventType.VIEW,
                                2L
                        ),
                        tuple(
                                20L,
                                EventType.ADD_TO_CART,
                                1L
                        )
                );
    }

    @Test
    void naoDeveContarProdutoForaDosIdsInformados() {
        analyticsEventRepository.saveAllAndFlush(
                List.of(
                        createEvent(
                                1L,
                                10L,
                                EventType.VIEW
                        ),
                        createEvent(
                                1L,
                                99L,
                                EventType.VIEW
                        )
                )
        );

        List<AnalyticsEventCountProjection> result =
                analyticsEventRepository
                        .countEventsByProductIds(
                                List.of(10L)
                        );

        assertThat(result)
                .extracting(
                        AnalyticsEventCountProjection::getProductId
                )
                .containsExactly(10L);
    }

    private AnalyticsEvent createEvent(
            Long landingPageId,
            Long productId,
            EventType eventType
    ) {
        AnalyticsEvent event = new AnalyticsEvent();

        event.setLandingPageId(landingPageId);
        event.setProductId(productId);
        event.setEventType(eventType);

        return event;
    }
}
