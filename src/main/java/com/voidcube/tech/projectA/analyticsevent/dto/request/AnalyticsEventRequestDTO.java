package com.voidcube.tech.projectA.analyticsevent.dto.request;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnalyticsEventRequestDTO(
    @NotNull(message = "{validation.analytics.landing-page-id.required}") @Positive(message = "{validation.analytics.landing-page-id.positive}") Long landingPageId,
    @NotNull(message = "validation.analytics.product-id.required}") @Positive(message = "{validation.analytics.product-id.positive}" ) Long productId,
    @NotNull(message = "{validation.analytics.event-type.required}") EventType eventType
) {
    
}
