package com.voidcube.tech.projectA.tenant.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTenantPlanRequest(
    @NotNull(message = "O ID do plano é obrigatório") Long planId
) {}
