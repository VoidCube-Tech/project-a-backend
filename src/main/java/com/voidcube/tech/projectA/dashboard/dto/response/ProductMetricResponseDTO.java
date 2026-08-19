package com.voidcube.tech.projectA.dashboard.dto.response;

public record ProductMetricResponseDTO(
    Long productId,
    String productName,
    Long eventCount
) {
    
}
