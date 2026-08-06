package com.voidcube.tech.projectA.product.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductVariationTest {

    @Test
    void deveAdicionarVariacaoAoProduto() {
        Product product = new Product();

        ProductVariation variation = new ProductVariation();
        variation.setVariationName("Cor");
        variation.setVariationValue("Azul");
        variation.setStockQuantity(10);

        product.addVariation(variation);

        assertTrue(
                product.getVariations().contains(variation)
        );

        assertSame(
                product,
                variation.getProduct()
        );
    }

    @Test
    void deveRemoverVariacaoDoProduto() {
        Product product = new Product();

        ProductVariation variation = new ProductVariation();
        variation.setVariationName("Cor");
        variation.setVariationValue("Azul");
        variation.setStockQuantity(10);

        product.addVariation(variation);
        product.removeVariation(variation);

        assertFalse(
                product.getVariations().contains(variation)
        );

        assertNull(variation.getProduct());
    }
}
