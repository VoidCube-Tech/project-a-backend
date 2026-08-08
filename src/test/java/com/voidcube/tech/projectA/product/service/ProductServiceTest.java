package com.voidcube.tech.projectA.product.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.product.dto.request.ProductRequestDTO;
import com.voidcube.tech.projectA.shared.exception.InvalidProductException;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductTagService productTagService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductService productService;

    @Test
    void deveRejeitarProdutoFisicoSemEstoque() {
        Tenant tenant = new Tenant();
        tenant.setId(10L);

        User admin = new User();
        admin.setId(5L);
        admin.setEmail("admin@teste.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setTenant(tenant);

        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenReturn(admin);

        ProductRequestDTO request =
                new ProductRequestDTO(
                        "Produto físico",
                        new BigDecimal("99.90"),
                        "Descrição",
                        ProductType.PHYSICAL,
                        null,
                        List.of(),
                        List.of()
                );

        assertThrows(
                InvalidProductException.class,
                () -> productService.create(request)
        );

        verify(productRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }
}
