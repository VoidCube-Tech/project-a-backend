package com.voidcube.tech.projectA.audit.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.dto.AuditLogResponse;
import com.voidcube.tech.projectA.audit.model.AuditLog;
import com.voidcube.tech.projectA.audit.repository.AuditLogRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final TenantRepository tenantRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public void register(
            String action,
            String entityName,
            String entityId
    ) {
        User user = authenticatedUserProvider.getAuthenticatedUser();

        saveAuditLog(
                action,
                entityName,
                entityId,
                user,
                user.getTenant()
        );
    }

    @Transactional
    public void register(
            String action,
            String entityName,
            String entityId,
            Long affectedTenantId
    ) {
        if (affectedTenantId == null) {
            throw new IllegalArgumentException(
                    "O ID do tenant afetado não pode ser nulo"
            );
        }

        User user = authenticatedUserProvider.getAuthenticatedUser();

        Tenant affectedTenant = tenantRepository.findById(affectedTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tenant afetado não encontrado: " + affectedTenantId
                ));

        validateTenantAccess(user, affectedTenant);
        saveAuditLog(action, entityName, entityId, user, affectedTenant);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(AuditLogResponse::from);
    }

    private void validateTenantAccess(User user, Tenant affectedTenant) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return;
        }

        Tenant userTenant = user.getTenant();

        if (userTenant == null
                || !userTenant.getId().equals(affectedTenant.getId())) {
            throw new AccessDeniedException(
                    "Admin não pode registrar ação de outro tenant"
            );
        }
    }

    private void saveAuditLog(
            String action,
            String entityName,
            String entityId,
            User user,
            Tenant affectedTenant
    ) {
        validateText(action, "action");
        validateText(entityName, "entityName");
        validateText(entityId, "entityId");

        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedByUser(user);
        auditLog.setTenant(affectedTenant);

        auditLogRepository.save(auditLog);
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode estar vazio"
            );
        }
    }
}