package com.voidcube.tech.projectA.audit.dto;

import java.time.LocalDateTime;

import com.voidcube.tech.projectA.audit.model.AuditLog;
import com.voidcube.tech.projectA.user.model.Role;

public record AuditLogResponse(
    Long id,
    String action,
    String entityName,
    String entityId,
    Long performedByUserId,
    Role performerdByRole,
    Long tenantId,
    AuditScope scope,
    LocalDateTime createdAt

) 
{
    public static AuditLogResponse from(AuditLog auditLog) {
        Role actorRole = auditLog.getPerformedByUser().getRole();
        Long affectedTenantId = auditLog.getTenant() != null
        ? auditLog.getTenant().getId()
        : null;

        return new AuditLogResponse(
            auditLog.getId(),
            auditLog.getAction(),
            auditLog.getEntityName(),
            auditLog.getEntityId(),
            auditLog.getPerformedByUser().getId(),
            actorRole,
            affectedTenantId,
            determineScope(actorRole, affectedTenantId),
            auditLog.getCreatedAt()
        );
    }
    
    private static AuditScope determineScope(
        Role actorRole,
        Long affectedTenantId
    ) {
        if (actorRole == Role.ROLE_SUPER_ADMIN) {
            return affectedTenantId == null
            ? AuditScope.GLOBAL
            : AuditScope.THIRD_PARTY_TENANT;
        }
        return AuditScope.OWN_TENANT;
    }
    
}
