package com.voidcube.tech.projectA.audit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.audit.model.AuditLog;

public interface AuditLogRepository extends JpaRepository <AuditLog, Long>{

    @Override
    @EntityGraph(attributePaths = {
        "performedByUser",
        "tenant"
    })
    Page<AuditLog> findAll(Pageable pageable);
    
}
