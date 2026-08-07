package com.voidcube.tech.projectA.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.product.model.ProductTag;

public interface ProductTagRepository extends JpaRepository <ProductTag, Long> {
    
    Optional<ProductTag> findByTenant_IdAndNameIgnoreCase(Long tenantId, String name);
}
