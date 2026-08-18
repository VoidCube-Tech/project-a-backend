package com.voidcube.tech.projectA.tenant.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.shared.exception.PlanNotFoundException;
import com.voidcube.tech.projectA.plan.model.Plan;
import com.voidcube.tech.projectA.plan.repository.PlanRepository;
import com.voidcube.tech.projectA.shared.exception.TenantNotFoundException;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdminTenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminTenantService adminTenantService;

    @Test
    void shouldUpdateTenantPlanAndRegisterAudit() {
        Long tenantId = 10L;
        Long planId = 2L;

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        Plan plan = new Plan();
        plan.setId(planId);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(planRepository.findById(planId))
                .thenReturn(Optional.of(plan));

        adminTenantService.updatePlan(
                tenantId,
                planId
        );

        assertThat(tenant.getPlan())
                .isSameAs(plan);

        InOrder operationOrder = inOrder(
                tenantRepository,
                auditLogService
        );

        operationOrder
                .verify(tenantRepository)
                .save(tenant);

        operationOrder
                .verify(auditLogService)
                .register(
                        "TENANT_PLAN_CHANGE",
                        "Tenant",
                        tenantId.toString(),
                        tenantId
                );
    }

    @Test
    void shouldThrowWhenTenantDoesNotExist() {
        Long tenantId = 10L;
        Long planId = 2L;

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adminTenantService.updatePlan(
                        tenantId,
                        planId
                )
        )
                .isInstanceOf(
                        TenantNotFoundException.class
                )
                .hasMessage(
                        "Tenant não encontrado: "
                                + tenantId
                );

        verifyNoInteractions(
                planRepository,
                auditLogService
        );

        verify(tenantRepository, never())
                .save(any(Tenant.class));
    }

    @Test
    void shouldThrowWhenPlanDoesNotExist() {
        Long tenantId = 10L;
        Long planId = 999L;

        Tenant tenant = new Tenant();
        tenant.setId(tenantId);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(planRepository.findById(planId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> adminTenantService.updatePlan(
                        tenantId,
                        planId
                )
        )
                .isInstanceOf(
                        PlanNotFoundException.class
                )
                .hasMessage(
                        "Plano não encontrado: "
                                + planId
                );

        verify(tenantRepository, never())
                .save(any(Tenant.class));

        verifyNoInteractions(auditLogService);
    }
}