package com.voidcube.tech.projectA.audit.service;

import com.voidcube.tech.projectA.audit.dto.AuditLogResponse;
import com.voidcube.tech.projectA.audit.model.AuditLog;
import com.voidcube.tech.projectA.audit.repository.AuditLogRepository;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import com.voidcube.tech.projectA.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

   
    @Transactional
    public void register(
            String action,
            String entityName,
            String entityId
    ) {
        User authenticatedUser = getAuthenticatedUser();

        saveAuditLog(
                action,
                entityName,
                entityId,
                authenticatedUser,
                authenticatedUser.getTenant()
        );
    }

    
    @Transactional
    public void register(
            String action,
            String entityName,
            String entityId,
            Long affectedTenantId
    ) {
        User authenticatedUser = getAuthenticatedUser();

        if (affectedTenantId == null) {
            throw new IllegalArgumentException(
                    "O ID do tenant afetado não pode ser nulo"
            );
        }

        Tenant affectedTenant = tenantRepository
                .findById(affectedTenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tenant afetado não encontrado: "
                                + affectedTenantId
                ));

        validateTenantAccess(
                authenticatedUser,
                affectedTenant
        );

        saveAuditLog(
                action,
                entityName,
                entityId,
                authenticatedUser,
                affectedTenant
        );
    }

  
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(
            Pageable pageable
    ) {
        return auditLogRepository
                .findAll(pageable)
                .map(AuditLogResponse::from);
    }

    
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                    instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(
                    "Não existe usuário autenticado"
            );
        }

        String authenticatedEmail = authentication.getName();

        return userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário autenticado não encontrado: "
                                + authenticatedEmail
                ));
    }

    private void validateTenantAccess(
            User authenticatedUser,
            Tenant affectedTenant
    ) {
        if (authenticatedUser.getRole()
                == Role.ROLE_SUPER_ADMIN) {
            return;
        }

        Tenant userTenant = authenticatedUser.getTenant();

        if (userTenant == null
                || !userTenant.getId()
                    .equals(affectedTenant.getId())) {
            throw new AccessDeniedException(
                    "Admin não pode registrar ação de outro tenant"
            );
        }
    }

    private void saveAuditLog(
            String action,
            String entityName,
            String entityId,
            User authenticatedUser,
            Tenant affectedTenant
    ) {
        validateText(action, "action");
        validateText(entityName, "entityName");
        validateText(entityId, "entityId");

        AuditLog auditLog = new AuditLog();

        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedByUser(authenticatedUser);
        auditLog.setTenant(affectedTenant);

        auditLogRepository.save(auditLog);
    }

    private void validateText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " não pode estar vazio"
            );
        }
    }
}