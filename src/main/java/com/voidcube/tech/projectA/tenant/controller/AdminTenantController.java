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
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/tenants")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminTenantController {

    private final AdminTenantService adminTenantService;

    @GetMapping
    public ResponseEntity<List<TenantResponse>> findAll() {
        List<TenantResponse> response = adminTenantService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{tenantId}/plan")
    public ResponseEntity<Void> updatePlan(
            @PathVariable Long tenantId,
            @Valid @RequestBody UpdateTenantPlanRequest request
    ) {
        adminTenantService.updatePlan(tenantId, request.planId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}