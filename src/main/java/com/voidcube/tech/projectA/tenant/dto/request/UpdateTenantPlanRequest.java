package com.voidcube.tech.projectA.tenant.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTenantPlanRequest(
    @NotNull(message = "O ID do plano é obrigatório") @Positive Long planId
) {}
