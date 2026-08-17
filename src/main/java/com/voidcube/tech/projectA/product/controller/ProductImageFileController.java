package com.voidcube.tech.projectA.product.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.product.dto.response.ProductImageFileResponseDTO;
import com.voidcube.tech.projectA.product.service.ProductImageFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/images")
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageFileController {
    
    private final ProductImageFileService productImageFileService;

    @GetMapping("/{imageId}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long productId, @PathVariable Long imageId) {
        ProductImageFileResponseDTO file = productImageFileService.findAdminFile(productId, imageId);

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(file.mediaType())
            .cacheControl(CacheControl.noStore())
            .body(file.resource());
    }
}
