package com.voidcube.tech.projectA.landingpage.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.landingpage.dto.response.PublicLandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.service.PublicLandingPageService;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/landing-pages")
public class PublicLandingPageController {

    private final PublicLandingPageService publicLandingPageService;

    @GetMapping("/{domainUrl}")
    public ResponseEntity<PublicLandingPageResponseDTO> findByDomainUrl(
            @PathVariable String domainUrl
    ) {
        PublicLandingPageResponseDTO response =
                publicLandingPageService.findByDomainUrl(domainUrl);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{domainUrl}/whatsapp")
    public ResponseEntity<Void> redirectToWhatsapp(
            @PathVariable String domainUrl,
            @RequestParam(required = false)
            @Positive(message = "{validation.analytics.product-id.positive}")
            Long productId
    ) {
        URI redirect = publicLandingPageService.buildWhatsappRedirect(
                domainUrl,
                productId
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirect)
                .build();
    }
}