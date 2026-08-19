package com.voidcube.tech.projectA.dashboard.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.projection.AnalyticsEventCountProjection;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;
import com.voidcube.tech.projectA.dashboard.dto.response.DashboardMetricsResponseDTO;
import com.voidcube.tech.projectA.dashboard.dto.response.ProductMetricResponseDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardMetricsService {

    private final ProductRepository productRepository;

    private final AnalyticsEventRepository
            analyticsEventRepository;

    private final AuthenticatedUserProvider
            authenticatedUserProvider;

    @Transactional(readOnly = true)
    public DashboardMetricsResponseDTO getMetrics() {
        Long tenantId =
                authenticatedUserProvider
                        .getRequiredTenantId();

        List<Product> products =
                productRepository
                        .findAllByTenant_Id(tenantId);

        if (products.isEmpty()) {
            return new DashboardMetricsResponseDTO(
                    List.of(),
                    List.of()
            );
        }

        Map<Long, Product> productsById =
                products.stream()
                        .collect(
                                Collectors.toMap(
                                        Product::getId,
                                        Function.identity()
                                )
                        );

        List<AnalyticsEventCountProjection> counts =
                analyticsEventRepository
                        .countEventsByProductIds(
                                productsById.keySet()
                        );

        List<ProductMetricResponseDTO> mostViewed =
                mapMetrics(
                        counts,
                        EventType.VIEW,
                        productsById
                );

        List<ProductMetricResponseDTO> mostAddedToCart =
                mapMetrics(
                        counts,
                        EventType.ADD_TO_CART,
                        productsById
                );

        return new DashboardMetricsResponseDTO(
                mostViewed,
                mostAddedToCart
        );
    }

    private List<ProductMetricResponseDTO> mapMetrics(
            List<AnalyticsEventCountProjection> counts,
            EventType eventType,
            Map<Long, Product> productsById
    ) {
        return counts.stream()
                .filter(count ->
                        count.getEventType() == eventType
                )
                .map(count -> {
                    Product product =
                            productsById.get(
                                    count.getProductId()
                            );

                    return new ProductMetricResponseDTO(
                            product.getId(),
                            product.getName(),
                            count.getEventCount()
                    );
                })
                .toList();
    }
}