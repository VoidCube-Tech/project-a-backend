package com.voidcube.tech.projectA.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.product.dto.response.ProductImageResponseDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductImage;
import com.voidcube.tech.projectA.product.repository.ProductImageRepository;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.exception.ImageStorageException;
import com.voidcube.tech.projectA.shared.exception.ProductImageNotFoundException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.shared.storage.ImageStorageService;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public ProductImageResponseDTO upload(
            Long productId,
            MultipartFile file,
            boolean requestedAsMain
    ) {
        Tenant tenant = getAuthenticatedTenant();
        Product product = findProduct(productId, tenant.getId());

        String storedPath = imageStorageService.save(file);
        registerFileCleanupOnRollback(storedPath);

        boolean hasMainImage =
                productImageRepository.countMainByProductId(productId) > 0;

        boolean shouldBeMain = requestedAsMain || !hasMainImage;

        if (shouldBeMain) {
            productImageRepository.clearMainImage(productId);
        }

        ProductImage image = new ProductImage();
        image.setImageUrl(storedPath);
        image.setMain(shouldBeMain);
        image.setProduct(product);

        ProductImage savedImage = productImageRepository.saveAndFlush(image);

        auditLogService.register(
                "PRODUCT_IMAGE_UPLOAD",
                "ProductImage",
                savedImage.getId().toString()
        );

        return ProductImageResponseDTO.from(savedImage);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponseDTO> findAll(Long productId) {
        Long tenantId = getAuthenticatedTenant().getId();

        findProduct(productId, tenantId);

        return productImageRepository.findAllByProductId(productId)
                .stream()
                .map(ProductImageResponseDTO::from)
                .toList();
    }

    @Transactional
    public ProductImageResponseDTO setMain(Long productId, Long imageId) {
        Long tenantId = getAuthenticatedTenant().getId();

        findProduct(productId, tenantId);

        ProductImage image = findImage(imageId, productId);

        if (image.isMain()) {
            return ProductImageResponseDTO.from(image);
        }

        productImageRepository.clearMainImage(productId);
        image.setMain(true);

        ProductImage savedImage = productImageRepository.saveAndFlush(image);

        auditLogService.register(
                "PRODUCT_IMAGE_SET_MAIN",
                "ProductImage",
                savedImage.getId().toString()
        );

        return ProductImageResponseDTO.from(savedImage);
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        Long tenantId = getAuthenticatedTenant().getId();

        findProduct(productId, tenantId);

        ProductImage image = findImage(imageId, productId);
        boolean removedImageWasMain = image.isMain();
        String storedPath = image.getImageUrl();

        productImageRepository.delete(image);
        productImageRepository.flush();

        if (removedImageWasMain) {
            promoteFirstRemainingImage(productId);
        }

        auditLogService.register(
                "PRODUCT_IMAGE_DELETE",
                "ProductImage",
                imageId.toString()
        );

        registerFileDeletionAfterCommit(storedPath);
    }

    private void promoteFirstRemainingImage(Long productId) {
        Optional<ProductImage> firstImage =
                productImageRepository.findFirstByProduct_IdOrderByIdAsc(
                        productId
                );

        if (firstImage.isEmpty()) {
            return;
        }

        ProductImage newMainImage = firstImage.get();
        newMainImage.setMain(true);
        productImageRepository.saveAndFlush(newMainImage);
    }

    private Product findProduct(Long productId, Long tenantId) {
        return productRepository.findByIdAndTenant_Id(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private ProductImage findImage(Long imageId, Long productId) {
        return productImageRepository.findByIdAndProduct_Id(imageId, productId)
                .orElseThrow(() ->
                        new ProductImageNotFoundException(imageId));
    }

    private Tenant getAuthenticatedTenant() {
        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getTenant() == null) {
            throw new AccessDeniedException(
                    "O usuário autenticado não possui tenant."
            );
        }

        return user.getTenant();
    }

    private void registerFileCleanupOnRollback(String storedPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            safelyDeleteFile(storedPath);
                        }
                    }
                }
        );
    }

    private void registerFileDeletionAfterCommit(String storedPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            safelyDeleteFile(storedPath);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        safelyDeleteFile(storedPath);
                    }
                }
        );
    }

    private void safelyDeleteFile(String storedPath) {
        try {
            imageStorageService.delete(storedPath);
        } catch (ImageStorageException exception) {
            log.error(
                    "Não foi possível remover o arquivo físico: {}",
                    storedPath,
                    exception
            );
        }
    }
}