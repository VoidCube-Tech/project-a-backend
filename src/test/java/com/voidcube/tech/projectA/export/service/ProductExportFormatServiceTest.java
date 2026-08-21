package com.voidcube.tech.projectA.export.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.voidcube.tech.projectA.export.dto.ProductExportDTO;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductTag;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductExportServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthenticatedUserProvider
            authenticatedUserProvider;

    @InjectMocks
    private ProductExportService
            productExportService;

    @Test
    void shouldExportActiveAndRemovedProducts() {
        Long tenantId = 5L;

        Product activeProduct = createProduct(
                10L,
                "Camiseta",
                null
        );

        Product removedProduct = createProduct(
                20L,
                "Caneca",
                LocalDateTime.now()
        );

        ProductTag tag = new ProductTag();
        tag.setName("Promoção");

        activeProduct.addTag(tag);

        when(
                authenticatedUserProvider
                        .getRequiredTenantId()
        ).thenReturn(tenantId);

        when(
                productRepository
                        .findAllIncludingDeletedByTenantId(
                                tenantId
                        )
        ).thenReturn(
                List.of(
                        activeProduct,
                        removedProduct
                )
        );

        List<ProductExportDTO> result =
                productExportService.findAll();

        assertThat(result).hasSize(2);

        assertThat(result.get(0).status())
                .isEqualTo("ATIVO");

        assertThat(result.get(0).tags())
                .containsExactly("Promoção");

        assertThat(result.get(1).status())
                .isEqualTo("REMOVIDO");

        verify(productRepository)
                .findAllIncludingDeletedByTenantId(
                        tenantId
                );
    }

    @Test
    void shouldGenerateCsvWithEscapedValues() {
        ProductExportDTO product =
                new ProductExportDTO(
                        10L,
                        "Camiseta, especial",
                        "Descrição com \"aspas\"",
                        new BigDecimal("99.90"),
                        ProductType.PHYSICAL,
                        5,
                        List.of("Roupa", "Promoção"),
                        "ATIVO"
                );

        byte[] result =
                productExportService.generateCsv(
                        List.of(product)
                );

        String csv =
                new String(
                        result,
                        StandardCharsets.UTF_8
                );

        assertThat(csv)
                .startsWith(
                        "id,nome,descrição,preço,"
                                + "tipo,estoque,tags,status\n"
                );

        assertThat(csv)
                .contains(
                        "\"Camiseta, especial\""
                );

        assertThat(csv)
                .contains(
                        "\"Descrição com \"\"aspas\"\"\""
                );

        assertThat(csv)
                .contains(
                        "\"Roupa; Promoção\""
                );
    }

    @Test
    void shouldGenerateOnlyHeaderWhenListIsEmpty() {
        byte[] result =
                productExportService.generateCsv(
                        List.of()
                );

        String csv =
                new String(
                        result,
                        StandardCharsets.UTF_8
                );

        assertThat(csv)
                .isEqualTo(
                        "id,nome,descrição,preço,"
                                + "tipo,estoque,tags,status\n"
                );
    }

    private Product createProduct(
            Long id,
            String name,
            LocalDateTime deletedAt
    ) {
        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setDescription("Descrição");
        product.setPrice(
                new BigDecimal("50.00")
        );
        product.setProductType(
                ProductType.PHYSICAL
        );
        product.setStockQuantity(10);
        product.setDeletedAt(deletedAt);

        return product;
    }
}
