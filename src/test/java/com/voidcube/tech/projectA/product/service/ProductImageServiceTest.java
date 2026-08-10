package com.voidcube.tech.projectA.product.service;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.product.dto.response.ProductImageResponseDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductImage;
import com.voidcube.tech.projectA.product.repository.ProductImageRepository;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.shared.storage.ImageStorageService;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductImageService productImageService;

    @Test
    void deveSalvarPrimeiraImagemComoPrincipal() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);
        Product product = createProduct(20L, tenant);

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "produto.png",
                        "image/png",
                        new byte[]{1, 2, 3}
                );

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(productRepository
                .findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));

        when(imageStorageService.save(file))
                .thenReturn("imagem.png");

        when(productImageRepository
                .countMainByProductId(20L))
                .thenReturn(0L);

        when(productImageRepository
                .saveAndFlush(any(ProductImage.class)))
                .thenAnswer(invocation -> {
                    ProductImage image =
                            invocation.getArgument(0);

                    image.setId(30L);

                    return image;
                });

        ProductImageResponseDTO response =
                productImageService.upload(
                        20L,
                        file,
                        false
                );

        assertEquals(30L, response.id());
        assertTrue(response.isMain());

        verify(auditLogService).register(
                "PRODUCT_IMAGE_UPLOAD",
                "ProductImage",
                "30"
        );
    }

    @Test
    void naoDeveAuditarListagem() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);
        Product product = createProduct(20L, tenant);

        ProductImage image = new ProductImage();
        image.setId(30L);
        image.setImageUrl("imagem.png");
        image.setMain(true);
        image.setProduct(product);

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(productRepository
                .findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));

        when(productImageRepository
                .findAllByProductId(20L))
                .thenReturn(List.of(image));

        List<ProductImageResponseDTO> response =
                productImageService.findAll(20L);

        assertEquals(1, response.size());

        verify(
                auditLogService,
                never()
        ).register(
                any(),
                any(),
                any()
        );
    }

    @Test
    void devePromoverOutraImagemAoRemoverPrincipal() {
        Tenant tenant = createTenant(10L);
        User admin = createAdmin(tenant);
        Product product = createProduct(20L, tenant);

        ProductImage mainImage = new ProductImage();
        mainImage.setId(30L);
        mainImage.setImageUrl("principal.png");
        mainImage.setMain(true);
        mainImage.setProduct(product);

        ProductImage remainingImage = new ProductImage();
        remainingImage.setId(31L);
        remainingImage.setImageUrl("secundaria.png");
        remainingImage.setMain(false);
        remainingImage.setProduct(product);

        when(authenticatedUserProvider
                .getAuthenticatedUser())
                .thenReturn(admin);

        when(productRepository
                .findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));

        when(productImageRepository
                .findByIdAndProduct_Id(30L, 20L))
                .thenReturn(Optional.of(mainImage));

        when(productImageRepository
                .findFirstByProduct_IdOrderByIdAsc(20L))
                .thenReturn(Optional.of(remainingImage));

        productImageService.delete(20L, 30L);

        assertTrue(remainingImage.isMain());

        verify(productImageRepository)
                .delete(mainImage);

        verify(productImageRepository)
                .saveAndFlush(remainingImage);

        verify(auditLogService).register(
                "PRODUCT_IMAGE_DELETE",
                "ProductImage",
                "30"
        );
    }

    private Tenant createTenant(Long id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);

        return tenant;
    }

    private User createAdmin(Tenant tenant) {
        User admin = new User();

        admin.setId(5L);
        admin.setEmail("admin@teste.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setTenant(tenant);

        return admin;
    }

    private Product createProduct(
            Long id,
            Tenant tenant
    ) {
        Product product = new Product();

        product.setId(id);
        product.setTenant(tenant);

        return product;
    }
}