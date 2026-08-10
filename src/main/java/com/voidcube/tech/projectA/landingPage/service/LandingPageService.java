package com.voidcube.tech.projectA.landingPage.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.landingPage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.landingPage.dto.response.LandingPageResponseDTO;
import com.voidcube.tech.projectA.landingPage.model.LandingPage;
import com.voidcube.tech.projectA.landingPage.repository.LandingPageRepository;
import com.voidcube.tech.projectA.shared.exception.DomainUrlAlreadyException;
import com.voidcube.tech.projectA.shared.exception.InvalidPageException;
import com.voidcube.tech.projectA.shared.exception.LandingPageNotFoundException;
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

        auditLogService.register("LANDING_PAGE_CREATE", "LandingPage", savedLandingPage.getId().toString());

        return LandingPageResponseDTO.from(savedLandingPage);

    }

    @Transactional(readOnly = true)
    public Page<LandingPageResponseDTO> findAll(Pageable pageable) {
        Tenant tenant = getAuthenticatedTenant();

        return landingPageRepository.findAllByTenant_id(tenant.getId(), pageable).map(LandingPageResponseDTO::from);
    }

    @Transactional
    public LandingPageResponseDTO update(Long landingPageId, LandingPageRequestDTO request) {
        Tenant tenant = getAuthenticatedTenant();

        LandingPage landingPage = landingPageRepository.findByIdAndTenant_Id(landingPageId, tenant.getId())
            .orElseThrow(()-> new LandingPageNotFoundException(landingPageId));
        
        String normalizedDomain = normalizeDomain(request.domainUrl());

        validateDomainForUpdate(normalizedDomain, landingPageId);

        landingPage.setName(request.name().trim());
        landingPage.setDomainUrl(normalizedDomain);
        landingPage.setWhatsappNumber(request.whatsappNumber());

        LandingPage savedLandingPage = saveLandingPage(landingPage);

        auditLogService.register("LANDING_PAGE_UPDATE", "LandingPage", savedLandingPage.getId().toString());

        return LandingPageResponseDTO.from(savedLandingPage);
    }

    private void validateDomainForCreation(String domainUrl) {
        boolean domainAlreadyExists = landingPageRepository.existsByDomainUrlIgnoreCase(domainUrl);

        if(domainAlreadyExists) {
            throw new DomainUrlAlreadyException(domainUrl);
        }
    }

    private void validateDomainForUpdate(String domainUrl, Long landingPageId) {
        boolean usedByAnotherLandingPage = landingPageRepository.existsByDomainUrlIgnoreCaseAndIdNot(domainUrl, landingPageId);

        if(usedByAnotherLandingPage) {
            throw new DomainUrlAlreadyException(domainUrl);
        }
    }

    private String normalizeDomain(String domainUrl) {
        return domainUrl
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private LandingPage saveLandingPage(LandingPage landingPage) {
        try{
            
            return landingPageRepository.saveAndFlush(landingPage);
            
        } catch (DataIntegrityViolationException exception) {
            
            throw new DomainUrlAlreadyException(landingPage.getDomainUrl());
        }
    }
    private String normalizeWhatsapp(String whatsappNumber) {
        if (whatsappNumber == null || whatsappNumber.isBlank()) {
            return null;
        }

        String digitsOnly = whatsappNumber.replaceAll("\\D", "");

        if (digitsOnly.length() < 8 || digitsOnly.length() > 15) {
            throw new InvalidPageException("O número do whatsapp deve possuir entre 8 a 15 dígitos");
        }

        return digitsOnly;
    }

    private Tenant getAuthenticatedTenant() {
        User authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();

        if(authenticatedUser.getTenant() == null) {
            throw new AccessDeniedException("o usuário autenticado não possui Tenant.");
        }
        return authenticatedUser.getTenant();
    }
    

}
