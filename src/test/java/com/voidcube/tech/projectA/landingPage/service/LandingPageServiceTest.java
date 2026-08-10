package com.voidcube.tech.projectA.landingPage.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.landingPage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.landingPage.dto.response.LandingPageResponseDTO;
import com.voidcube.tech.projectA.landingPage.model.LandingPage;
import com.voidcube.tech.projectA.landingPage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandingPageServiceTest {

    @Mock
    private LandingPageRepository landingPageRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LandingPageService landingPageService;

    @Test
    void deveCriarLandingPageParaTenantAutenticado() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(landingPageRepository
                .existsByDomainUrlIgnoreCase(
                        "minha-loja"
                ))
                .thenReturn(false);

        when(landingPageRepository
                .saveAndFlush(any(LandingPage.class)))
                .thenAnswer(invocation -> {
                    LandingPage landingPage =
                            invocation.getArgument(0);

                    landingPage.setId(20L);

                    return landingPage;
                });

        LandingPageRequestDTO request =
                new LandingPageRequestDTO(
                        "Minha Loja",
                        "MINHA-LOJA",
                        "(91) 99999-9999"
                );

        LandingPageResponseDTO response =
                landingPageService.create(request);

        assertEquals(20L, response.id());
        assertEquals(
                "minha-loja",
                response.domainUrl()
        );
        assertEquals(
                "91999999999",
                response.whatsappNumber()
        );

        verify(auditLogService).register(
                "LANDING_PAGE_CREATE",
                "LandingPage",
                "20"
        );
    }

    @Test
    void naoDeveAuditarListagem() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);

        LandingPage landingPage = new LandingPage();
        landingPage.setId(20L);
        landingPage.setName("Minha Loja");
        landingPage.setDomainUrl("minha-loja");
        landingPage.setTenant(tenant);

        PageRequest pageable =
                PageRequest.of(0, 20);

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(landingPageRepository
                .findAllByTenant_id(
                        10L,
                        pageable
                ))
                .thenReturn(
                        new PageImpl<>(
                                List.of(landingPage)
                        )
                );

        Page<LandingPageResponseDTO> response =
                landingPageService.findAll(pageable);

        assertEquals(1, response.getTotalElements());

        verify(
                auditLogService,
                never()
        ).register(
                any(),
                any(),
                any()
        );
    }

    @Test
    void naoDeveAtualizarLandingPageDeOutroTenant() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(landingPageRepository
                .findByIdAndTenant_Id(99L, 10L))
                .thenReturn(Optional.empty());

        LandingPageRequestDTO request =
                new LandingPageRequestDTO(
                        "Outra Loja",
                        "outra-loja",
                        null
                );

        assertThrows(
                LandingPageNotFoundException.class,
                () -> landingPageService.update(
                        99L,
                        request
                )
        );
    }

    private Tenant createTenant(Long id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);

        return tenant;
    }

    private User createAdmin(Tenant tenant) {
        User admin = new User();

        admin.setId(5L);
        admin.setEmail("admin@teste.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setTenant(tenant);

        return admin;
    }
}
