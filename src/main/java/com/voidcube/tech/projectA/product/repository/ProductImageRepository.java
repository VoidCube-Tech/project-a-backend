package com.voidcube.tech.projectA.product.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.product.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
            SELECT image
            FROM ProductImage image
            WHERE image.product.id = :productId
            ORDER BY image.id ASC
            """)

    List<ProductImage> findAllByProductId(@Param("productId") Long productId);

    Optional<ProductImage> findByIdAndProduct_Id(Long imageId, Long productId);
    
    Optional<ProductImage> findFirstByProduct_IdOrderByIdAsc(Long productId);

    @Query("""
            SELECT COUNT(image)
            FROM ProductImage image
            WHERE image.product.id = :productId
                AND image.isMain = true
            """)
    long countMainByProductId(@Param("productId")Long productId);

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE ProductImage image
            SET image.isMain = false
            WHERE image.product.id = :productId
            """)
    int clearMainImage(@Param("productId") Long productId);
}
