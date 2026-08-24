package com.voidcube.tech.projectA.tenant.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateTenantPlanRequest(
    @NotNull(message = "{validation.tenant.plan-id.required}") @Positive(message = "{validation.tenant.plan-id.positive}" ) Long planId
) {}
