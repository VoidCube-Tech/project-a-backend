package com.voidcube.tech.projectA.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.product.dto.request.ProductRequestDTO;
import com.voidcube.tech.projectA.product.dto.request.ProductVariationRequestDTO;
import com.voidcube.tech.projectA.product.dto.response.ProductResponseDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductTag;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.model.ProductVariation;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.exception.InvalidProductException;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTagService productTagService;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Tenant tenant = getAuthenticatedTenant();

        validateStockRule(request);

        Product product = new Product();
        product.setTenant(tenant);
        applyRequest(product, request, tenant);

        Product savedProduct = productRepository.save(product);

        auditLogService.register(
                "PRODUCT_CREATE",
                "Product",
                savedProduct.getId().toString()
        );

        return ProductResponseDTO.from(savedProduct);
    }

    @Transactional
    public Page<ProductResponseDTO> findAll(Pageable pageable) {
        Long tenantId = getAuthenticatedTenant().getId();

        Page<ProductResponseDTO> response =
                productRepository.findAllByTenant_Id(tenantId, pageable)
                        .map(ProductResponseDTO::from);

        auditLogService.register("PRODUCT_LIST", "Product", "*");
        return response;
    }

    @Transactional
    public ProductResponseDTO findById(Long productId) {
        Long tenantId = getAuthenticatedTenant().getId();
        Product product = findProduct(productId, tenantId);
        ProductResponseDTO response = ProductResponseDTO.from(product);

        auditLogService.register(
                "PRODUCT_VIEW",
                "Product",
                productId.toString()
        );

        return response;
    }

    @Transactional
    public ProductResponseDTO update(
            Long productId,
            ProductRequestDTO request
    ) {
        Tenant tenant = getAuthenticatedTenant();
        Product product = findProduct(productId, tenant.getId());

        validateStockRule(request);
        applyRequest(product, request, tenant);

        Product savedProduct = productRepository.save(product);

        auditLogService.register(
                "PRODUCT_UPDATE",
                "Product",
                savedProduct.getId().toString()
        );

        return ProductResponseDTO.from(savedProduct);
    }

    @Transactional
    public void delete(Long productId) {
        Long tenantId = getAuthenticatedTenant().getId();
        Product product = findProduct(productId, tenantId);

        product.markAsDeleted();
        productRepository.save(product);

        auditLogService.register(
                "PRODUCT_DELETE",
                "Product",
                productId.toString()
        );
    }

    private void applyRequest(
            Product product,
            ProductRequestDTO request,
            Tenant tenant
    ) {
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setProductType(request.productType());
        product.setStockQuantity(request.stockQuantity());
        product.replaceVariations(createVariations(request.variations()));

        Set<ProductTag> tags =
                productTagService.findOrCreateForTenant(request.tags(), tenant);

        product.replaceTags(tags);
    }

    private List<ProductVariation> createVariations(
            List<ProductVariationRequestDTO> requests
    ) {
        if (requests == null) {
            return List.of();
        }

        List<ProductVariation> variations = new ArrayList<>();

        for (ProductVariationRequestDTO request : requests) {
            ProductVariation variation = new ProductVariation();

            variation.setVariationName(request.variationName().trim());
            variation.setVariationValue(request.variationValue().trim());
            variation.setStockQuantity(request.stockQuantity());

            variations.add(variation);
        }

        return variations;
    }

    private void validateStockRule(ProductRequestDTO request) {
        if (request.productType() == ProductType.PHYSICAL
                && request.stockQuantity() == null) {
            throw new InvalidProductException(
                    "A quantidade em estoque é obrigatória para produtos físicos."
            );
        }
    }

    private Product findProduct(Long productId, Long tenantId) {
        return productRepository.findByIdAndTenant_Id(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
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
}