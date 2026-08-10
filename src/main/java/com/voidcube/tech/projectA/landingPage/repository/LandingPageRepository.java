package com.voidcube.tech.projectA.landingPage.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.landingPage.model.LandingPage;

public interface LandingPageRepository extends JpaRepository<LandingPage, Long> {
    
    Page <LandingPage> findAllByTenant_id(Long tenantId,  Pageable pageable);

    Optional<LandingPage> findByIdAndTenant_Id(Long landingPageId, Long tenantId);

    boolean existsByDomainUrlIgnoreCase(String domainUrl);

    boolean existsByDomainUrlIgnoreCaseAndIdNot(String domainUrl, Long landingPageId);
}
