package com.voidcube.tech.projectA.tenant.dto.response;

import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.model.Tier;
import java.time.LocalDateTime;

public record TenantResponse(
        Long id,
        String companyName,
        Tier tier,
        LocalDateTime createdAt
) {

    public static TenantResponse from(
            Tenant tenant
    ) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getCompanyName(),
                tenant.getTier(),
                tenant.getCreatedAt()
        );
    }
}
