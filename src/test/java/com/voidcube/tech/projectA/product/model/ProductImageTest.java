package com.voidcube.tech.projectA.product.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductImageTest {

    @Test
    void deveRelacionarImagemAoProduto() {
        Product product = createProduct();

        ProductImage image = createImage(
                "images/product-1.png",
                false
        );

        product.addImage(image);

        assertTrue(product.getImages().contains(image));
        assertSame(product, image.getProduct());
    }

    @Test
    void deveDefinirPrimeiraImagemComoPrincipal() {
        Product product = createProduct();

        ProductImage firstImage = createImage(
                "images/product-1.png",
                false
        );

        ProductImage secondImage = createImage(
                "images/product-2.png",
                false
        );

        product.addImage(firstImage);
        product.addImage(secondImage);

        product.validateProduct();

        assertTrue(firstImage.isMain());
        assertFalse(secondImage.isMain());
    }

    @Test
    void deveManterImagemPrincipalInformada() {
        Product product = createProduct();

        ProductImage firstImage = createImage(
                "images/product-1.png",
                false
        );

        ProductImage secondImage = createImage(
                "images/product-2.png",
                true
        );

        product.addImage(firstImage);
        product.addImage(secondImage);

        product.validateProduct();

        assertFalse(firstImage.isMain());
        assertTrue(secondImage.isMain());
    }

    @Test
    void deveRejeitarMaisDeUmaImagemPrincipal() {
        Product product = createProduct();

        ProductImage firstImage = createImage(
                "images/product-1.png",
                true
        );

        ProductImage secondImage = createImage(
                "images/product-2.png",
                true
        );

        product.addImage(firstImage);
        product.addImage(secondImage);

        assertThrows(
                IllegalStateException.class,
                product::validateProduct
        );
    }

    @Test
    void deveTrocarImagemPrincipal() {
        Product product = createProduct();

        ProductImage firstImage = createImage(
                "images/product-1.png",
                true
        );

        ProductImage secondImage = createImage(
                "images/product-2.png",
                false
        );

        product.addImage(firstImage);
        product.addImage(secondImage);

        product.defineMainImage(secondImage);

        assertFalse(firstImage.isMain());
        assertTrue(secondImage.isMain());
    }

    @Test
    void deveEscolherOutraPrincipalAoRemoverAtual() {
        Product product = createProduct();

        ProductImage firstImage = createImage(
                "images/product-1.png",
                true
        );

        ProductImage secondImage = createImage(
                "images/product-2.png",
                false
        );

        product.addImage(firstImage);
        product.addImage(secondImage);

        product.removeImage(firstImage);

        assertFalse(product.getImages().contains(firstImage));
        assertNull(firstImage.getProduct());
        assertTrue(secondImage.isMain());
        assertEquals(1, product.getImages().size());
    }

    private Product createProduct() {
        Product product = new Product();

        product.setName("Produto de teste");
        product.setPrice(new BigDecimal("99.90"));
        product.setProductType(ProductType.DIGITAL);

        return product;
    }

    private ProductImage createImage(
            String imageUrl,
            boolean isMain
    ) {
        ProductImage image = new ProductImage();

        image.setImageUrl(imageUrl);
        image.setMain(isMain);

        return image;
    }
}
