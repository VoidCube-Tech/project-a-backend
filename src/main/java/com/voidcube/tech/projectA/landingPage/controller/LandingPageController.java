package com.voidcube.tech.projectA.landingPage.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.landingPage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.landingPage.dto.response.LandingPageResponseDTO;
import com.voidcube.tech.projectA.landingPage.service.LandingPageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/landing-pages")
@PreAuthorize("hasRole('ADMIN')")
public class LandingPageController {
    
    private final LandingPageService landingPageService;

    @PostMapping
    public ResponseEntity<LandingPageResponseDTO> createLandingPage(
        @Valid @RequestBody LandingPageRequestDTO request
    ) {
        LandingPageResponseDTO responseDTO = landingPageService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<LandingPageResponseDTO>> listLandingPages(
        @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<LandingPageResponseDTO> responseDTO = landingPageService.findAll(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandingPageResponseDTO> updateLandingPage(
        @PathVariable("id") Long id,
        @Valid @RequestBody LandingPageRequestDTO requestDTO
    ) {
        LandingPageResponseDTO responseDTO = landingPageService.update(id, requestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @PostMapping("/{pageId}/products/{productId}")
    public ResponseEntity<Void> associateProduct(
        @PathVariable Long pageId,
        @PathVariable Long productId
    ) {
        boolean created = landingPageService.associateProduct(pageId, productId);

        if (!created) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{pageId}/products/{productId}")
    public ResponseEntity<Void> disassociateProduct(
        @PathVariable Long pageId,
        @PathVariable Long productId
    ) {
        landingPageService.disassociateProduct(pageId, productId);

        return ResponseEntity.noContent().build();
    }
}
