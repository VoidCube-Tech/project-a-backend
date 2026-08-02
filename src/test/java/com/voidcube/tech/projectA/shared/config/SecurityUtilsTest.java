package com.voidcube.tech.projectA.shared.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

public class SecurityUtilsTest {
    
    private final SecurityUtils securityUtils =
        new SecurityUtils();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAuthenticatedTenantId() {
        Tenant tenant = new Tenant();
        tenant.setId(42L);

        User user = new User();
        user.setTenant(tenant);

        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(
            new TestingAuthenticationToken(
                user,
                null,
                "ROLE_ADMIN"
            )
        );

        SecurityContextHolder.setContext(context);

        Long tenantId =
            securityUtils.requireAuthenticatedTenantId();

        assertEquals(42L, tenantId);
    }
}
