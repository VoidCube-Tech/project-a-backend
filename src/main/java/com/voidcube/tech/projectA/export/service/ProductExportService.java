package com.voidcube.tech.projectA.export.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.export.dto.ProductExportDTO;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class ProductExportService {
    
    private final ProductRepository productRepository;
    
    private static final String CSV_HEADER = "id,nome,descrição,preço,tipo," + "estoque,tags,status\n";

    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public List<ProductExportDTO> findAll() {
        Long tenantId = authenticatedUserProvider.getRequiredTenantId();

        return productRepository.findAllIncludingDeletedByTenantId(tenantId)
            .stream()
            .map(ProductExportDTO::from)
            .toList();
    }

    public byte[] generateCsv(List<ProductExportDTO> products) {
        StringBuilder csv = new StringBuilder(CSV_HEADER);

        for(ProductExportDTO product: products) {
            csv.append(product.id())
                .append(",");

            csv.append(escapeCsv(product.nome()))
                .append(",");

            csv.append(escapeCsv(product.descricao()))
                .append(",");

            csv.append(product.preco() == null ? "" : product
                .preco()
                .toPlainString())
                .append(",");

            csv.append(product.tipo() == null ? null : product
                .tipo()
                .name())
                .append(",");
            
            csv.append(product.estoque() == null ? "" :product.estoque())
                .append(",");

            String tags = product.tags()
                .stream()
                .collect(Collectors.joining("; "));
            
            csv.append(escapeCsv(tags))
                .append(",");
            
            csv.append(escapeCsv(product.status()))
                .append("\n");
        }
            return csv
                .toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if(value == null) {
            return "";
        }

        String escapedValue = value.replace("\"", "\"\"");

        return "\"" + escapedValue + "\"";
    }
}
