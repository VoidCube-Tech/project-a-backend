package com.voidcube.tech.projectA.dashboard.dto.response;

import java.util.List;

public record DashboardMetricsResponseDTO(
    List<ProductMetricResponseDTO> mostViewedProducts,
    List<ProductMetricResponseDTO> mostAddedToCartProducts
) {
    
}
