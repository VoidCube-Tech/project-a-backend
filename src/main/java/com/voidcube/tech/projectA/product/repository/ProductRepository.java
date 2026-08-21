package com.voidcube.tech.projectA.product.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findAllByTenant_Id(Long TenantId, Pageable pageable);

    List<Product> findAllByTenant_Id(Long tenantId);

    Optional<Product> findByIdAndTenant_Id(Long productId, Long tenantId);

    @Query(value = """
            SELECT product.*
            FROM product
            WHERE product.tenant_id = :tenantId
            ORDER BY product.id
            """, nativeQuery = true)
    List<Product> findAllIncludingDeletedByTenantId(@Param("tenantId") Long tenantId);
}
