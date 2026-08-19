package com.voidcube.tech.projectA.analyticsevent.dto.request;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;

import jakarta.validation.constraints.NotNull;

public record AnalyticsEventRequestDTO(
    @NotNull(message = "O ID da landing page é obrigatório") Long landingPageId,
    @NotNull(message = "O ID de produto é obrigatório") Long productId,
    @NotNull(message = "O tipo do evento é indispensável") EventType eventType
) {
    
}
