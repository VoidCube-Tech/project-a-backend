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
import org.springframework.web.bind.annotation.PutMapping;
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
        PromotionResponseDTO response = promotionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PromotionResponseDTO>> findAll(
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<PromotionResponseDTO> response = promotionService.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PromotionRequestDTO request
    ) {
        PromotionResponseDTO response = promotionService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{promotionId}/products/{productId}")
    public ResponseEntity<Void> associateProduct(
            @PathVariable Long promotionId,
            @PathVariable Long productId
    ) {
        boolean associated =
                promotionService.associateProduct(promotionId, productId);

        HttpStatus status = associated
                ? HttpStatus.CREATED
                : HttpStatus.NO_CONTENT;

        return ResponseEntity.status(status).build();
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