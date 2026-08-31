package com.voidcube.tech.projectA.product.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findAllByTenant_Id(Long TenantId, Pageable pageable);

    List<Product> findAllByTenant_Id(Long tenantId);

    Optional<Product> findByIdAndTenant_Id(Long productId, Long tenantId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE product
            SET stock_quantity = stock_quantity - :quantity
            WHERE id = :productId
              AND tenant_id = :tenantId
              AND deleted_at IS NULL
              AND product_type = 'PHYSICAL'
              AND stock_quantity >= :quantity
            """, nativeQuery = true)
    int decrementStock(
            @Param("productId") Long productId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE product
            SET stock_quantity = stock_quantity + :quantity
            WHERE id = :productId
              AND tenant_id = :tenantId
              AND product_type = 'PHYSICAL'
            """, nativeQuery = true)
    int restoreStock(
            @Param("productId") Long productId,
            @Param("tenantId") Long tenantId,
            @Param("quantity") Integer quantity
    );

    @Query(value = """
            SELECT product.*
            FROM product
            WHERE product.tenant_id = :tenantId
            ORDER BY product.id
            """, nativeQuery = true)
    List<Product> findAllIncludingDeletedByTenantId(
        @Param("tenantId") Long tenantId);
}
