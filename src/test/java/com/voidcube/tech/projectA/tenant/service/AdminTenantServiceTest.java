package com.voidcube.tech.projectA.tenant.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.plan.model.Plan;
import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminTenantService adminTenantService;

    @Test
    void deveListarTodosOsTenantsERegistrarAuditoria() {
        Plan basicPlan = createPlan(
                1L,
                "Basic"
        );

        Tenant firstTenant = createTenant(
                10L,
                "Loja A",
                basicPlan
        );

        Tenant secondTenant = createTenant(
                20L,
                "Loja B",
                basicPlan
        );

        when(tenantRepository.findAll())
                .thenReturn(List.of(
                        firstTenant,
                        secondTenant
                ));

        List<TenantResponse> response =
                adminTenantService.findAll();

        assertEquals(2, response.size());

        assertEquals(
                firstTenant.getId(),
                response.get(0).id()
        );

        assertEquals(
                firstTenant.getCompanyName(),
                response.get(0).companyName()
        );

        assertEquals(
                basicPlan.getId(),
                response.get(0).planId()
        );

        assertEquals(
                secondTenant.getId(),
                response.get(1).id()
        );

        assertEquals(
                secondTenant.getCompanyName(),
                response.get(1).companyName()
        );

        assertEquals(
                basicPlan.getId(),
                response.get(1).planId()
        );

        verify(tenantRepository).findAll();

        verify(auditLogService).register(
                "LIST_ALL",
                "Tenant",
                "ALL"
        );
    }

    private Plan createPlan(
            Long id,
            String name
    ) {
        Plan plan = new Plan();

        plan.setId(id);
        plan.setName(name);

        return plan;
    }

    private Tenant createTenant(
            Long id,
            String companyName,
            Plan plan
    ) {
        Tenant tenant = new Tenant();

        tenant.setId(id);
        tenant.setCompanyName(companyName);
        tenant.setPlan(plan);
        tenant.setCreatedAt(LocalDateTime.now());

        return tenant;
    }
}