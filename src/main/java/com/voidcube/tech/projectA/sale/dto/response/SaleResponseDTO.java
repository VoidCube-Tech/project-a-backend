package com.voidcube.tech.projectA.sale.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.voidcube.tech.projectA.sale.model.Sale;
import com.voidcube.tech.projectA.sale.model.SaleStatus;

public record SaleResponseDTO(
    Long id,
    String customerName,
    String customerPhone,
    SaleStatus status,
    BigDecimal totalAmount,
    Long registeredByUserId,
    LocalDateTime createdAt,
    LocalDateTime cancelledAt,
    Long cancelledByUserId,
    List<SaleItemResponseDTO> items
) {
    
    public static SaleResponseDTO from(Sale sale) {
        List<SaleItemResponseDTO> items = sale.getItems()
            .stream()
            .map(SaleItemResponseDTO::from)
            .toList();
        
        return new SaleResponseDTO(
            sale.getId(),
            sale.getCustomerName(),
            sale.getCustomerPhone(),
            sale.getStatus(),
            sale.getTotalAmount(),
            sale.getRegisteredByUserId(),
            sale.getCreatedAt(),
            sale.getCancelledAt(),
            sale.getCancelledByUserId(),
            items
        );
    }
}
