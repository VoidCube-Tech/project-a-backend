package com.voidcube.tech.projectA.product.service;

import java.util.Locale;


import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.product.dto.response.ProductImageFileResponseDTO;
import com.voidcube.tech.projectA.product.model.ProductImage;
import com.voidcube.tech.projectA.product.repository.ProductImageRepository;
import com.voidcube.tech.projectA.shared.exception.ProductImageNotFoundException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.shared.storage.ImageStorageService;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageFileService {
    
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public ProductImageFileResponseDTO findAdminFile(Long productId, Long imageId) {
        Long tenantId = getAuthenticatedTenantId();

        ProductImage image = productImageRepository.findAdminFile(imageId, productId, tenantId)
            .orElseThrow(() -> new ProductImageNotFoundException(imageId));

            return loadFile(image);
    }

    @Transactional(readOnly = true)
    public ProductImageFileResponseDTO findPublicFile(Long imageId) {
        ProductImage image = productImageRepository.findPublishedFile(imageId)
            .filter(foundImage -> foundImage.getProduct().isAvailable())
            .orElseThrow(() -> new ProductNotFoundException(imageId));

            return loadFile(image);
    }

    private ProductImageFileResponseDTO loadFile(ProductImage image) {
        String storagePath = image.getImageUrl();

        MediaType mediaType = resolveContentType(storagePath);

        Resource resource = imageStorageService.fetch(storagePath);

        return new ProductImageFileResponseDTO(resource, mediaType);
    }

    private MediaType resolveContentType(String storagePath) {
        String normalizedPath = storagePath.toLowerCase(Locale.ROOT);

        if(normalizedPath.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }

        if(normalizedPath.endsWith(".jpg")) {
            return MediaType.IMAGE_JPEG;
        }

        if(normalizedPath.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }

        throw new IllegalStateException("Tipo de imagem armazenada não suportado");
    }

    private Long getAuthenticatedTenantId() {
        User authenticatedUser = authenticatedUserProvider.getAuthenticatedUser();

        Tenant tenant = authenticatedUser.getTenant();

        if(tenant == null) {
        throw new AccessDeniedException("A operação exige um usuário vinculado");
        }
        return tenant.getId();
    }
}
