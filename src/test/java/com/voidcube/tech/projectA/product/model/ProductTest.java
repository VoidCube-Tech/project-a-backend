package com.voidcube.tech.projectA.product.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void deveRejeitarProdutoFisicoSemEstoque() {
        Product product = new Product();
        product.setProductType(ProductType.PHYSICAL);
        product.setPrice(new BigDecimal("50.00"));
        product.setStockQuantity(null);

        assertThrows(
                IllegalStateException.class,
                product::validateProduct
        );
    }

    @Test
    void deveAceitarProdutoFisicoComEstoque() {
        Product product = new Product();
        product.setProductType(ProductType.PHYSICAL);
        product.setPrice(new BigDecimal("50.00"));
        product.setStockQuantity(10);

        assertDoesNotThrow(product::validateProduct);
    }

    @Test
    void deveAceitarProdutoDigitalSemEstoque() {
        Product product = new Product();
        product.setProductType(ProductType.DIGITAL);
        product.setPrice(new BigDecimal("29.90"));
        product.setStockQuantity(null);

        assertDoesNotThrow(product::validateProduct);
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        Product product = new Product();
        product.setProductType(ProductType.PHYSICAL);
        product.setPrice(new BigDecimal("50.00"));
        product.setStockQuantity(-1);

        assertThrows(
                IllegalStateException.class,
                product::validateProduct
        );
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        Product product = new Product();
        product.setProductType(ProductType.DIGITAL);
        product.setPrice(new BigDecimal("-10.00"));

        assertThrows(
                IllegalStateException.class,
                product::validateProduct
        );
    }
}
