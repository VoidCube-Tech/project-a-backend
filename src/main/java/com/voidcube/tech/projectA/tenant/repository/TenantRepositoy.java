package com.voidcube.tech.projectA.tenant.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.tenant.model.Tenant;

public interface TenantRepositoy extends JpaRepository<Tenant, Long> {
    
}
