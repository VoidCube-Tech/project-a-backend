package com.voidcube.tech.projectA.landingpage.service;

import java.util.ArrayList;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.landingpage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.LandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.model.LandingPage;
import com.voidcube.tech.projectA.landingpage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.exception.DomainUrlAlreadyException;
import com.voidcube.tech.projectA.shared.exception.InvalidPageException;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LandingPageService {

    private final LandingPageRepository landingPageRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;
    private final ProductRepository productRepository;

    @Transactional
    public LandingPageResponseDTO create(LandingPageRequestDTO request) {
        Tenant tenant = getAuthenticatedTenant();
        String normalizedDomain = normalizeDomain(request.domainUrl());

        validateDomainForCreation(normalizedDomain);

        LandingPage landingPage = new LandingPage();
        landingPage.setName(request.name().trim());
        landingPage.setDomainUrl(normalizedDomain);
        landingPage.setWhatsappNumber(normalizeWhatsapp(request.whatsappNumber()));
        landingPage.setTenant(tenant);

        LandingPage savedLandingPage = saveLandingPage(landingPage);

        auditLogService.register(
                "LANDING_PAGE_CREATE",
                "LandingPage",
                savedLandingPage.getId().toString()
        );

        return LandingPageResponseDTO.from(savedLandingPage);
    }

    @Transactional(readOnly = true)
    public Page<LandingPageResponseDTO> findAll(Pageable pageable) {
        Long tenantId = getAuthenticatedTenant().getId();

        return landingPageRepository.findAllByTenant_Id(tenantId, pageable)
                .map(LandingPageResponseDTO::from);
    }

    @Transactional
    public LandingPageResponseDTO update(
            Long landingPageId,
            LandingPageRequestDTO request
    ) {
        Tenant tenant = getAuthenticatedTenant();
        LandingPage landingPage = findLandingPage(landingPageId, tenant.getId());
        String normalizedDomain = normalizeDomain(request.domainUrl());

        validateDomainForUpdate(normalizedDomain, landingPageId);

        landingPage.setName(request.name().trim());
        landingPage.setDomainUrl(normalizedDomain);
        landingPage.setWhatsappNumber(normalizeWhatsapp(request.whatsappNumber()));

        LandingPage savedLandingPage = saveLandingPage(landingPage);

        auditLogService.register(
                "LANDING_PAGE_UPDATE",
                "LandingPage",
                savedLandingPage.getId().toString()
        );

        return LandingPageResponseDTO.from(savedLandingPage);
    }

    @Transactional
    public void delete(Long landingPageId) {
        Tenant tenant = getAuthenticatedTenant();
        LandingPage landingPage = findLandingPage(landingPageId, tenant.getId());

        new ArrayList<>(landingPage.getProducts()).forEach(landingPage::removeProduct);

        landingPageRepository.delete(landingPage);
        landingPageRepository.flush();

        auditLogService.register(
                "LANDING_PAGE_DELETE",
                "LandingPage",
                landingPageId.toString()
        );
    }

    @Transactional
    public boolean associateProduct(Long pageId, Long productId) {
        Tenant tenant = getAuthenticatedTenant();
        LandingPage landingPage = findLandingPage(pageId, tenant.getId());
        Product product = findProduct(productId, tenant.getId());

        if (!landingPage.addProduct(product)) {
            return false;
        }

        landingPageRepository.saveAndFlush(landingPage);
        auditLogService.register(
                "LANDING_PAGE_PRODUCT_ASSOCIATE",
                "LandingPageProduct",
                pageId + ":" + productId
        );

        return true;
    }

    @Transactional
    public boolean disassociateProduct(Long pageId, Long productId) {
        Tenant tenant = getAuthenticatedTenant();
        LandingPage landingPage = findLandingPage(pageId, tenant.getId());
        Product product = findProduct(productId, tenant.getId());

        if (!landingPage.removeProduct(product)) {
            return false;
        }

        landingPageRepository.saveAndFlush(landingPage);
        auditLogService.register(
                "LANDING_PAGE_PRODUCT_DISASSOCIATE",
                "LandingPageProduct",
                pageId + ":" + productId
        );

        return true;
    }

    private void validateDomainForCreation(String domainUrl) {
        if (landingPageRepository.existsByDomainUrlIgnoreCase(domainUrl)) {
            throw new DomainUrlAlreadyException(domainUrl);
        }
    }

    private void validateDomainForUpdate(String domainUrl, Long landingPageId) {
        boolean usedByAnotherPage =
                landingPageRepository.existsByDomainUrlIgnoreCaseAndIdNot(
                        domainUrl,
                        landingPageId
                );

        if (usedByAnotherPage) {
            throw new DomainUrlAlreadyException(domainUrl);
        }
    }

    private LandingPage saveLandingPage(LandingPage landingPage) {
        try {
            return landingPageRepository.saveAndFlush(landingPage);
        } catch (DataIntegrityViolationException exception) {
            throw new DomainUrlAlreadyException(landingPage.getDomainUrl());
        }
    }

    private String normalizeDomain(String domainUrl) {
        return domainUrl.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeWhatsapp(String whatsappNumber) {
        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            return null;
        }

        String digitsOnly = whatsappNumber.replaceAll("\\D", "");

        if (digitsOnly.length() < 8 || digitsOnly.length() > 15) {
            throw new InvalidPageException(
                    "O número do WhatsApp deve possuir entre 8 e 15 dígitos."
            );
        }

        return digitsOnly;
    }

    private LandingPage findLandingPage(Long pageId, Long tenantId) {
        return landingPageRepository.findByIdAndTenant_Id(pageId, tenantId)
                .orElseThrow(() -> new LandingPageNotFoundException(pageId));
    }

    private Product findProduct(Long productId, Long tenantId) {
        return productRepository.findByIdAndTenant_Id(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Tenant getAuthenticatedTenant() {
        User authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();

        if (authenticatedUser.getTenant() == null) {
            throw new AccessDeniedException(
                    "O usuário autenticado não possui tenant."
            );
        }

        return authenticatedUser.getTenant();
    }
}