package com.voidcube.tech.projectA.tenant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.plan.model.Plan;
import com.voidcube.tech.projectA.plan.repository.PlanRepository;
import com.voidcube.tech.projectA.shared.exception.PlanNotFoundException;
import com.voidcube.tech.projectA.shared.exception.TenantNotFoundException;
import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTenantService {
    
    private final TenantRepository tenantRepository;
    private final PlanRepository planRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public List<TenantResponse> findAll() {
        List<TenantResponse> tenants = tenantRepository.findAll()
            .stream()
            .map(TenantResponse::from)
            .toList();

    auditLogService.register("LIST_ALL", "Tenant", "ALL");

        return tenants;
    }

    @Transactional
    public void updatePlan(Long tenantId, Long planId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException(tenantId));

        Plan plan = planRepository.findById(planId)
            .orElseThrow(() -> new PlanNotFoundException(planId));

            tenant.setPlan(plan);

            tenantRepository.save(tenant);

            auditLogService.register("TENANT_PLAN_CHANGE", "Tenant", tenantId.toString(), tenantId);
    }
}
