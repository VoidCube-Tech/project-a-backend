package com.voidcube.tech.projectA.tenant.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.tenant.dto.response.TenantResponse;
import com.voidcube.tech.projectA.tenant.service.AdminTenantService;

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
}
