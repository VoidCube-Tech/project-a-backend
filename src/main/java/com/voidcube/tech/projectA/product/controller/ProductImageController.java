package com.voidcube.tech.projectA.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.voidcube.tech.projectA.product.dto.response.ProductImageResponseDTO;
import com.voidcube.tech.projectA.product.service.ProductImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/{productId}/images")
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageResponseDTO> uploadImage(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "isMain", defaultValue = "false") boolean isMain
    ) {
        ProductImageResponseDTO response =
                productImageService.upload(productId, file, isMain);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductImageResponseDTO>> listImages(
            @PathVariable Long productId
    ) {
        List<ProductImageResponseDTO> response =
                productImageService.findAll(productId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{imageId}/main")
    public ResponseEntity<ProductImageResponseDTO> setMainImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        ProductImageResponseDTO response =
                productImageService.setMain(productId, imageId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        productImageService.delete(productId, imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}