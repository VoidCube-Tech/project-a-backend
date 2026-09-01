package com.voidcube.tech.projectA.landingpage.controller;

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

import com.voidcube.tech.projectA.landingpage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.landingpage.dto.response.LandingPageResponseDTO;
import com.voidcube.tech.projectA.landingpage.service.LandingPageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/landing-pages")
@PreAuthorize("hasRole('ADMIN')")
public class LandingPageController {

    private final LandingPageService landingPageService;

    @PostMapping
    public ResponseEntity<LandingPageResponseDTO> createLandingPage(
            @Valid @RequestBody LandingPageRequestDTO request
    ) {
        LandingPageResponseDTO response = landingPageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LandingPageResponseDTO>> listLandingPages(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<LandingPageResponseDTO> response = landingPageService.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandingPageResponseDTO> updateLandingPage(
            @PathVariable Long id,
            @Valid @RequestBody LandingPageRequestDTO request
    ) {
        LandingPageResponseDTO response = landingPageService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        landingPageService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{pageId}/products/{productId}")
    public ResponseEntity<Void> associateProduct(
            @PathVariable Long pageId,
            @PathVariable Long productId
    ) {
        boolean associated = landingPageService.associateProduct(pageId, productId);
        HttpStatus status = associated ? HttpStatus.CREATED : HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).build();
    }

    @DeleteMapping("/{pageId}/products/{productId}")
    public ResponseEntity<Void> disassociateProduct(
            @PathVariable Long pageId,
            @PathVariable Long productId
    ) {
        landingPageService.disassociateProduct(pageId, productId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}