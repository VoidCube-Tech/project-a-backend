package com.voidcube.tech.projectA.export.dto;

import java.math.BigDecimal;
import java.util.List;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductTag;
import com.voidcube.tech.projectA.product.model.ProductType;

public record ProductExportDTO(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    ProductType tipo,
    Integer estoque,
    List<String> tags,
    String status
) {

    public static ProductExportDTO from(Product product) {
        List<String> tagNames = product.getTags()
            .stream()
            .map(ProductTag::getName)
            .sorted()
            .toList();

            String productStatus = product.getDeletedAt() == null
                ? "ATIVO"
                : "REMOVIDO";

            return new ProductExportDTO(product.getId(),product.getName(),product.getDescription(),product.getPrice(),product.getProductType(),product.getStockQuantity(), tagNames, productStatus);
    }
    
}
