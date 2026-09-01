package com.voidcube.tech.projectA.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.dashboard.dto.response.DashboardMetricsResponseDTO;
import com.voidcube.tech.projectA.dashboard.service.DashboardMetricsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard/metrics")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardMetricsController {

    private final DashboardMetricsService dashboardMetricsService;

    @GetMapping
    public ResponseEntity<DashboardMetricsResponseDTO> getMetrics() {
        DashboardMetricsResponseDTO response =
                dashboardMetricsService.getMetrics();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}