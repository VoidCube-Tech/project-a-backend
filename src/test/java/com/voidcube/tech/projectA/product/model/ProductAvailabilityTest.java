package com.voidcube.tech.projectA.product.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductAvailabilityTest {

    @Test
    void deveConsiderarProdutoDigitalSempreDisponivel() {
        Product product = new Product();

        product.setProductType(ProductType.DIGITAL);
        product.setStockQuantity(null);

        assertTrue(product.isAvailable());
    }

    @Test
    void deveConsiderarProdutoFisicoComEstoqueDisponivel() {
        Product product = new Product();

        product.setProductType(ProductType.PHYSICAL);
        product.setStockQuantity(5);

        assertTrue(product.isAvailable());
    }

    @Test
    void deveConsiderarProdutoFisicoComEstoqueZeroIndisponivel() {
        Product product = new Product();

        product.setProductType(ProductType.PHYSICAL);
        product.setStockQuantity(0);

        assertFalse(product.isAvailable());
    }

    @Test
    void deveConsiderarProdutoFisicoComEstoqueNuloIndisponivel() {
        Product product = new Product();

        product.setProductType(ProductType.PHYSICAL);
        product.setStockQuantity(null);

        assertFalse(product.isAvailable());
    }
}
