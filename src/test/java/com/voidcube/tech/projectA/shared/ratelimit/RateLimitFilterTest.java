package com.voidcube.tech.projectA.shared.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import tools.jackson.databind.json.JsonMapper;

class RateLimitFilterTest {

    private final RateLimitFilter filter =
            new RateLimitFilter(
                    JsonMapper.builder().build()
            );

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldBlockRequestAfterOneHundredRequestsByIp()
            throws Exception {
        String clientIp = "192.168.0.10";

        for (int requestNumber = 1;
                requestNumber <= 100;
                requestNumber++) {
            MockHttpServletResponse response =
                    performRequest(clientIp);

            assertEquals(200, response.getStatus());
        }

        MockHttpServletResponse blockedResponse =
                performRequest(clientIp);

        assertEquals(
                429,
                blockedResponse.getStatus()
        );

        assertEquals(
                "0",
                blockedResponse.getHeader(
                        "X-RateLimit-Remaining"
                )
        );

        assertTrue(
                blockedResponse
                        .getContentAsString()
                        .contains(
                                "Limite de requisições excedido"
                        )
        );
    }

    @Test
    void shouldMaintainDifferentBucketsForDifferentIps()
            throws Exception {
        String firstIp = "192.168.0.20";
        String secondIp = "192.168.0.21";

        for (int requestNumber = 1;
                requestNumber <= 100;
                requestNumber++) {
            performRequest(firstIp);
        }

        MockHttpServletResponse firstIpResponse =
                performRequest(firstIp);

        MockHttpServletResponse secondIpResponse =
                performRequest(secondIp);

        assertEquals(
                429,
                firstIpResponse.getStatus()
        );

        assertEquals(
                200,
                secondIpResponse.getStatus()
        );
    }

    @Test
    void shouldUseTenantIdInsteadOfIpForAuthenticatedUser()
            throws Exception {
        Tenant tenant = new Tenant();
        tenant.setId(50L);

        User user = new User();
        user.setTenant(tenant);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        for (int requestNumber = 1;
                requestNumber <= 100;
                requestNumber++) {
            performRequest("192.168.0." + requestNumber);
        }

        MockHttpServletResponse response =
                performRequest("10.0.0.1");

        assertEquals(429, response.getStatus());
    }

    private MockHttpServletResponse performRequest(
            String remoteAddress
    ) throws Exception {
        MockHttpServletRequest request =
                new MockHttpServletRequest(
                        "GET",
                        "/api/v1/products"
                );

        request.setRemoteAddr(remoteAddress);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                new MockFilterChain()
        );

        return response;
    }
}
