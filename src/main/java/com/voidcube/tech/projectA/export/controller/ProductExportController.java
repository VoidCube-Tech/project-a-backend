package com.voidcube.tech.projectA.export.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.export.dto.ProductExportDTO;
import com.voidcube.tech.projectA.export.exception.InvalidExportFormatException;
import com.voidcube.tech.projectA.export.service.ProductExportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/export/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductExportController {

    private final ProductExportService productExportService;

    @GetMapping
    public ResponseEntity<?> export(@RequestParam String formato) {
        String normalizedFormat = normalizeFormat(formato);
        List<ProductExportDTO> products = productExportService.findAll();

        return normalizedFormat.equals("json")
                ? buildJsonResponse(products)
                : buildCsvResponse(products);
    }

    private ResponseEntity<List<ProductExportDTO>> buildJsonResponse(
            List<ProductExportDTO> products
    ) {
        String contentDisposition = ContentDisposition.attachment()
                .filename("products.json")
                .build()
                .toString();

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(products);
    }

    private ResponseEntity<byte[]> buildCsvResponse(
            List<ProductExportDTO> products
    ) {
        byte[] csv = productExportService.generateCsv(products);

        String contentDisposition = ContentDisposition.attachment()
                .filename("products.csv")
                .build()
                .toString();

        MediaType csvMediaType =
                MediaType.parseMediaType("text/csv;charset=UTF-8");

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(csvMediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(csv);
    }

    private String normalizeFormat(String format) {
        if (format == null) {
            throw new InvalidExportFormatException(null);
        }

        String normalized = format.trim().toLowerCase(Locale.ROOT);

        if (!normalized.equals("json") && !normalized.equals("csv")) {
            throw new InvalidExportFormatException(format);
        }

        return normalized;
    }
}