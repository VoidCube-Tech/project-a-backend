package com.voidcube.tech.projectA.analyticsevent.dto.request;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnalyticsEventRequestDTO(
    @NotNull(message = "O ID da landing page é obrigatório") @Positive Long landingPageId,
    @NotNull(message = "O ID de produto é obrigatório") @Positive Long productId,
    @NotNull(message = "O tipo do evento é indispensável") EventType eventType
) {
    
}
