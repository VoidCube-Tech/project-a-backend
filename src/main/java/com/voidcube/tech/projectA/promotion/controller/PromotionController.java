package com.voidcube.tech.projectA.promotion.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.promotion.dto.request.PromotionRequestDTO;
import com.voidcube.tech.projectA.promotion.dto.response.PromotionResponseDTO;
import com.voidcube.tech.projectA.promotion.service.PromotionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/promotions")
@PreAuthorize("hasRole('ADMIN')")
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponseDTO> create(
        @Valid @RequestBody PromotionRequestDTO request
    ) {
        PromotionResponseDTO created = promotionService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponseDTO>> findAll(
        @PageableDefault(
            size = 20,
            sort = "id",
            direction = Sort.Direction.DESC
        ) Pageable pageable
    ) {
        Page<PromotionResponseDTO> promotions = promotionService
            .findAll(pageable);

        return ResponseEntity.ok(promotions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{promotionId}/products/{productId}")
    public ResponseEntity<Void> associateProduct(
        @PathVariable Long productId,
        @PathVariable Long promotionId
    ) {
        boolean associated = promotionService.associateProduct(promotionId, productId);

        if(!associated) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{promotionId}/products/{productId}")
    public ResponseEntity<Void> disassociateProduct(
        @PathVariable Long promotionId,
        @PathVariable Long productId
    ) {
        promotionService.disassociateProduct(promotionId, productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
