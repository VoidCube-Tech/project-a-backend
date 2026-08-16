package com.voidcube.tech.projectA.landingpage.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.landingpage.model.LandingPage;

public interface LandingPageRepository extends JpaRepository<LandingPage, Long> {
    
    Page <LandingPage> findAllByTenant_id(Long tenantId,  Pageable pageable);

    Optional<LandingPage> findByIdAndTenant_Id(Long landingPageId, Long tenantId);

    boolean existsByDomainUrlIgnoreCase(String domainUrl);

    boolean existsByDomainUrlIgnoreCaseAndIdNot(String domainUrl, Long landingPageId);

    @Query("""
            SELECT DISTINCT landingPage
            FROM LandingPage landingPage
            LEFT JOIN FETCH landingPage.products
            WHERE landingPage.domainUrl = :domainUrl
            """)
    Optional<LandingPage> findPublicByDomainUrl(@Param("domainUrl")String domainUrl);
}
