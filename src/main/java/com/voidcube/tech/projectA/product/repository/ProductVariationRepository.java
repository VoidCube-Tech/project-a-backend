package com.voidcube.tech.projectA.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.product.model.ProductVariation;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    
    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE product_variation variation
            SET stock_quantity = variation.stock_quantity - :quantity
            FROM product
            WHERE variation.id = :variationId
              AND variation.product_id = :productId
              AND product.id = variation.product_id
              AND product.tenant_id = :tenantId
              AND product.deleted_at IS NULL
              AND product.product_type = 'PHYSICAL'
              AND variation.stock_quantity >= :quantity
            """, nativeQuery = true)
    int decrementStock(
            @Param("variationId") Long variationId,
            @Param("productId") Long productId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE product_variation variation
            SET stock_quantity = variation.stock_quantity + :quantity
            FROM product
            WHERE variation.id = :variationId
              AND variation.product_id = :productId
              AND product.id = variation.product_id
              AND product.tenant_id = :tenantId
              AND product.product_type = 'PHYSICAL'
            """, nativeQuery = true)
    int restoreStock(
            @Param("variationId") Long variationId,
            @Param("productId") Long productId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );
}
