package com.voidcube.tech.projectA.sale.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.sale.dto.request.SaleRequestDTO;
import com.voidcube.tech.projectA.sale.dto.response.SaleResponseDTO;
import com.voidcube.tech.projectA.sale.service.SaleService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasRole('ADMIN')")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponseDTO> create(
            @Valid @RequestBody SaleRequestDTO request
    ) {
        SaleResponseDTO response = saleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponseDTO>> findAll(
            @PageableDefault(
                    size = 20,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        Page<SaleResponseDTO> response = saleService.findAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponseDTO> findById(
            @PathVariable
            @Positive(message = "{validation.sale.id.positive}")
            Long id
    ) {
        SaleResponseDTO response = saleService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<SaleResponseDTO> cancel(
            @PathVariable
            @Positive(message = "{validation.sale.id.positive}")
            Long id
    ) {
        SaleResponseDTO response = saleService.cancel(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}