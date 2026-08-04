package com.voidcube.tech.projectA.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AuditLogTest {

    @Test
        void deveCriarAuditLogComOsDadosCorretos() {
        AuditLog auditLog = new AuditLog();

        auditLog.setAction("UPDATE");
        auditLog.setEntityName("Product");
        auditLog.setEntityId("123");

        assertEquals("UPDATE", auditLog.getAction());
        assertEquals("Product", auditLog.getEntityName());
        assertEquals("123", auditLog.getEntityId());
    }

    @Test
    void devePreencherCreatedAtAntesDePersistir() {
        AuditLog auditLog = new AuditLog();

        auditLog.onCreate();

        assertNotNull(auditLog.getCreatedAt());
    }

    @Test
    void deveAceitarEntityIdEmFormatoTexto() {
        AuditLog auditLog = new AuditLog();

        auditLog.setEntityId("product-abc-123");

        assertEquals(
            "product-abc-123",
            auditLog.getEntityId()
        );
}
}
