package com.voidcube.tech.projectA.landingpage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.service.PublicLandingPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/landing-pages")
public class PublicLandingPageController {
    
    private final PublicLandingPageService publicLandingPageService;

    @GetMapping("/{domainUrl}")
    public ResponseEntity<PublicLandingPageResponseDTO> findByDomainUrl(@PathVariable("domainUrl")String domainUrl) {
        PublicLandingPageResponseDTO response = publicLandingPageService.findByDomainUrl(domainUrl);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
