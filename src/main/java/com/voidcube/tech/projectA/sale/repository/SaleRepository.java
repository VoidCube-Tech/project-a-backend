package com.voidcube.tech.projectA.sale.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.sale.model.Sale;

import jakarta.persistence.LockModeType;

public interface SaleRepository extends JpaRepository <Sale, Long> {
    
    Page<Sale> findAllByTenant_Id(Long tenantId, Pageable pageable);

    Optional<Sale> findByIdAndTenant_Id(Long saleId, Long tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT sale
            FROM Sale sale
            WHERE sale.id = :saleId
              AND sale.tenant.id = :tenantId
            """)
    Optional<Sale> findByIdAndTenantIdForUpdate(
            @Param("saleId") Long saleId,
            @Param("tenantId") Long tenantId
    );
}
