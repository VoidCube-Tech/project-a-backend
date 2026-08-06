package com.voidcube.tech.projectA.audit.service;

import com.voidcube.tech.projectA.audit.model.AuditLog;
import com.voidcube.tech.projectA.audit.repository.AuditLogRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void deveRegistrarAcaoDoAdminNoProprioTenant() {
        Tenant tenant = createTenant(10L);

        User admin = createUser(
                5L,
                "admin@teste.com",
                Role.ROLE_ADMIN,
                tenant
        );

        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenReturn(admin);

        auditLogService.register(
                "CREATE",
                "Product",
                "50"
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository)
                .save(captor.capture());

        AuditLog savedAuditLog = captor.getValue();

        assertEquals(
                "CREATE",
                savedAuditLog.getAction()
        );

        assertEquals(
                "Product",
                savedAuditLog.getEntityName()
        );

        assertEquals(
                "50",
                savedAuditLog.getEntityId()
        );

        assertEquals(
                admin,
                savedAuditLog.getPerformedByUser()
        );

        assertEquals(
                tenant,
                savedAuditLog.getTenant()
        );
    }

    @Test
    void deveRegistrarAcaoDoSuperAdminEmOutroTenant() {
        User superAdmin = createUser(
                1L,
                "superadmin@voidcube.tech",
                Role.ROLE_SUPER_ADMIN,
                null
        );

        Tenant affectedTenant = createTenant(20L);

        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenReturn(superAdmin);

        when(tenantRepository.findById(20L))
                .thenReturn(Optional.of(affectedTenant));

        auditLogService.register(
                "PLAN_CHANGE",
                "Tenant",
                "20",
                20L
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository)
                .save(captor.capture());

        AuditLog savedAuditLog = captor.getValue();

        assertEquals(
                superAdmin,
                savedAuditLog.getPerformedByUser()
        );

        assertEquals(
                affectedTenant,
                savedAuditLog.getTenant()
        );
    }

    @Test
    void deveImpedirAdminDeRegistrarOutroTenant() {
        Tenant adminTenant = createTenant(10L);
        Tenant anotherTenant = createTenant(20L);

        User admin = createUser(
                5L,
                "admin@teste.com",
                Role.ROLE_ADMIN,
                adminTenant
        );

        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenReturn(admin);

        when(tenantRepository.findById(20L))
                .thenReturn(Optional.of(anotherTenant));

        assertThrows(
                AccessDeniedException.class,
                () -> auditLogService.register(
                        "UPDATE",
                        "Product",
                        "100",
                        20L
                )
        );

        verify(
                auditLogRepository,
                never()
        ).save(any(AuditLog.class));
    }

    private Tenant createTenant(Long id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);

        return tenant;
    }

    private User createUser(
            Long id,
            String email,
            Role role,
            Tenant tenant
    ) {
        User user = new User();

        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        user.setTenant(tenant);

        return user;
    }
}