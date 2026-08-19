package com.voidcube.tech.projectA.product.repository;

import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findAllByTenant_Id(Long TenantId, Pageable pageable);

    List<Product> findAllByTenant_Id(Long tenantId);

    Optional<Product> findByIdAndTenant_Id(Long productId, Long tenantId);
}
