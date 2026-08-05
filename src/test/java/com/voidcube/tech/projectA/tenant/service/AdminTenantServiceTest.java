package com.voidcube.tech.projectA.tenant.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Tenant tenant = new Tenant();

        tenant.setId(10L);
        tenant.setCompanyName("Loja Teste");

        when(tenantRepository.findAll())
                .thenReturn(List.of(tenant));

        List<TenantResponse> result =
                adminTenantService.findAll();

        assertEquals(1, result.size());

        assertEquals(
                "Loja Teste",
                result.getFirst().companyName()
        );

        verify(auditLogService).register(
                "LIST_ALL",
                "Tenant",
                "ALL"
        );
    }
}
