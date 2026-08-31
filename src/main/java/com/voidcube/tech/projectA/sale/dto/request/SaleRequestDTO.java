package com.voidcube.tech.projectA.sale.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaleRequestDTO(

    @Size(max = 255, message = "{validation.sale.customer-name.size}")
        String customerName,

        @Size(max = 30, message = "{validation.sale.customer-phone.size}")
        @Pattern(regexp = "^[0-9() +\\-]*$", message = "{validation.sale.customer-phone.format}")
        String customerPhone,

        @NotEmpty(message = "{validation.sale.items.required}")
        @Size(max = 100, message = "{validation.sale.items.size}")
        List<@Valid SaleItemRequestDTO> items
) {
    
}
