package com.voidcube.tech.projectA.audit.controller;

import com.voidcube.tech.projectA.audit.dto.AuditLogResponse;
import com.voidcube.tech.projectA.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public Page<AuditLogResponse> findAll(@PageableDefault(size = 20,sort = "createdAt",direction = DESC)Pageable pageable) {

        return auditLogService.findAll(pageable);
    }
}
