package com.voidcube.tech.projectA.product.controller;

import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.product.dto.response.ProductImageFileResponseDTO;
import com.voidcube.tech.projectA.product.service.ProductImageFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/product-images")
public class PublicProductImageFileController {
    
    private final ProductImageFileService productImageFileService;

    @GetMapping("/{imageId}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long imageId) {
        ProductImageFileResponseDTO file = productImageFileService.findPublicFile(imageId);

        return ResponseEntity.status(HttpStatus.OK)
            .contentType(file.mediaType())
            .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30))
            .cachePublic())
            .body(file.resource());
    }
}
