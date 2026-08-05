package com.voidcube.tech.projectA.tenant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminTenantService {
    
    private final TenantRepository tenantRepository;
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
}
