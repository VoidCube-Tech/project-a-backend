package com.voidcube.tech.projectA.tenant.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.tenant.dto.request.UpdateTenantPlanRequest;
import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.service.AdminTenantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/admin/tenants")
@RequiredArgsConstructor
public class AdminTenantController {
    
    private final AdminTenantService adminTenantService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<TenantResponse> findAll() {
        return adminTenantService.findAll();
    }

    @PutMapping("/{tenantId}/plan")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> updatePlan(@PathVariable Long tenantId, @Valid @RequestBody UpdateTenantPlanRequest request) {
        adminTenantService.updatePlan(tenantId, request.planId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
