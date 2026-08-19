package com.voidcube.tech.projectA.dashboard.service;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.projection.AnalyticsEventCountProjection;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;
import com.voidcube.tech.projectA.dashboard.dto.response.DashboardMetricsResponseDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardMetricsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AnalyticsEventRepository
            analyticsEventRepository;

    @Mock
    private AuthenticatedUserProvider
            authenticatedUserProvider;

    @Captor
    private ArgumentCaptor<Collection<Long>>
            productIdsCaptor;

    @InjectMocks
    private DashboardMetricsService
            dashboardMetricsService;

    @Test
    void shouldReturnMetricsForTenantProducts() {
        Long tenantId = 5L;

        Product firstProduct = new Product();
        firstProduct.setId(10L);
        firstProduct.setName("Camiseta");

        Product secondProduct = new Product();
        secondProduct.setId(20L);
        secondProduct.setName("Curso");

        when(
                authenticatedUserProvider
                        .getRequiredTenantId()
        ).thenReturn(tenantId);

        when(
                productRepository
                        .findAllByTenant_Id(tenantId)
        ).thenReturn(
                List.of(
                        firstProduct,
                        secondProduct
                )
        );

        AnalyticsEventCountProjection views =
                createCount(
                        10L,
                        EventType.VIEW,
                        12L
                );

        AnalyticsEventCountProjection carts =
                createCount(
                        20L,
                        EventType.ADD_TO_CART,
                        4L
                );

        when(
                analyticsEventRepository
                        .countEventsByProductIds(
                                anyCollection()
                        )
        ).thenReturn(
                List.of(views, carts)
        );

        DashboardMetricsResponseDTO response =
                dashboardMetricsService.getMetrics();

        assertThat(response.mostViewedProducts())
                .hasSize(1);

        assertThat(
                response
                        .mostViewedProducts()
                        .getFirst()
                        .productId()
        ).isEqualTo(10L);

        assertThat(
                response
                        .mostViewedProducts()
                        .getFirst()
                        .productName()
        ).isEqualTo("Camiseta");

        assertThat(
                response
                        .mostViewedProducts()
                        .getFirst()
                        .eventCount()
        ).isEqualTo(12L);

        assertThat(response.mostAddedToCartProducts())
                .hasSize(1);

        assertThat(
                response
                        .mostAddedToCartProducts()
                        .getFirst()
                        .productId()
        ).isEqualTo(20L);

        assertThat(
                response
                        .mostAddedToCartProducts()
                        .getFirst()
                        .productName()
        ).isEqualTo("Curso");

        assertThat(
                response
                        .mostAddedToCartProducts()
                        .getFirst()
                        .eventCount()
        ).isEqualTo(4L);
    }

    @Test
    void shouldReturnEmptyMetricsWhenTenantHasNoProducts() {
        Long tenantId = 5L;

        when(
                authenticatedUserProvider
                        .getRequiredTenantId()
        ).thenReturn(tenantId);

        when(
                productRepository
                        .findAllByTenant_Id(tenantId)
        ).thenReturn(List.of());

        DashboardMetricsResponseDTO response =
                dashboardMetricsService.getMetrics();

        assertThat(response.mostViewedProducts())
                .isEmpty();

        assertThat(response.mostAddedToCartProducts())
                .isEmpty();

        verifyNoInteractions(analyticsEventRepository);
    }

    @Test
    void shouldQueryOnlyAuthenticatedTenantProductIds() {
        Long tenantId = 5L;

        Product firstProduct = new Product();
        firstProduct.setId(10L);
        firstProduct.setName("Camiseta");

        Product secondProduct = new Product();
        secondProduct.setId(20L);
        secondProduct.setName("Curso");

        when(
                authenticatedUserProvider
                        .getRequiredTenantId()
        ).thenReturn(tenantId);

        when(
                productRepository
                        .findAllByTenant_Id(tenantId)
        ).thenReturn(
                List.of(
                        firstProduct,
                        secondProduct
                )
        );

        when(
                analyticsEventRepository
                        .countEventsByProductIds(
                                anyCollection()
                        )
        ).thenReturn(List.of());

        dashboardMetricsService.getMetrics();

        verify(productRepository)
                .findAllByTenant_Id(tenantId);

        verify(analyticsEventRepository)
                .countEventsByProductIds(
                        productIdsCaptor.capture()
                );

        assertThat(productIdsCaptor.getValue())
                .containsExactlyInAnyOrder(
                        10L,
                        20L
                );
    }

    private AnalyticsEventCountProjection createCount(
            Long productId,
            EventType eventType,
            Long eventCount
    ) {
        AnalyticsEventCountProjection projection =
                mock(
                        AnalyticsEventCountProjection.class
                );

        when(projection.getProductId())
                .thenReturn(productId);

        when(projection.getEventType())
                .thenReturn(eventType);

        when(projection.getEventCount())
                .thenReturn(eventCount);

        return projection;
    }
}