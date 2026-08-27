package com.voidcube.tech.projectA.promotion.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.promotion.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Page<Promotion> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<Promotion> findByIdAndTenant_Id(Long promotionId, Long tenantId);

    @Query("""
        SELECT CASE
                   WHEN COUNT(promotion) > 0
                   THEN true
                   ELSE false
               END
        FROM CouponPromotion promotion
        WHERE promotion.tenant.id = :tenantId
          AND LOWER(TRIM(promotion.couponCode)) =
              LOWER(TRIM(:couponCode))
        """)
    boolean existsCouponCodeByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("couponCode") String couponCode
    );

    @Query("""
        SELECT CASE
                   WHEN COUNT(promotion) > 0
                   THEN true
                   ELSE false
               END
        FROM CouponPromotion promotion
        WHERE promotion.tenant.id = :tenantId
          AND promotion.id <> :promotionId
          AND LOWER(TRIM(promotion.couponCode)) =
              LOWER(TRIM(:couponCode))
        """)
    boolean existsCouponCodeByTenantIdExcludingPromotionId(
            @Param("tenantId") Long tenantId,
            @Param("promotionId") Long promotionId,
            @Param("couponCode") String couponCode
    );
}