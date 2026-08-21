package com.voidcube.tech.projectA.export.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.voidcube.tech.projectA.export.exception.InvalidExportFormatException;
import com.voidcube.tech.projectA.export.service.ProductExportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProductExportControllerTest {

    @Test
    void shouldReturnJsonAsAttachment() {
        ProductExportService service =
                mock(ProductExportService.class);

        ProductExportController controller =
                new ProductExportController(service);

        when(service.findAll())
                .thenReturn(List.of());

        ResponseEntity<?> response =
                controller.export("json");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(
                response
                        .getHeaders()
                        .getContentType()
        ).isEqualTo(
                org.springframework.http
                        .MediaType.APPLICATION_JSON
        );

        assertThat(
                response
                        .getHeaders()
                        .getContentDisposition()
                        .getFilename()
        ).isEqualTo("products.json");

        verify(service).findAll();
    }

    @Test
    void shouldReturnCsvAsAttachment() {
        ProductExportService service =
                mock(ProductExportService.class);

        ProductExportController controller =
                new ProductExportController(service);

        when(service.findAll())
                .thenReturn(List.of());

        when(service.generateCsv(List.of()))
                .thenReturn(new byte[0]);

        ResponseEntity<?> response =
                controller.export("csv");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(
                response
                        .getHeaders()
                        .getContentDisposition()
                        .getFilename()
        ).isEqualTo("products.csv");

        verify(service).findAll();
        verify(service).generateCsv(List.of());
    }

    @Test
    void shouldRejectInvalidFormatBeforeDatabaseQuery() {
        ProductExportService service =
                mock(ProductExportService.class);

        ProductExportController controller =
                new ProductExportController(service);

        assertThatThrownBy(
                () -> controller.export("xml")
        )
                .isInstanceOf(
                        InvalidExportFormatException.class
                );

        verifyNoInteractions(service);
    }
}
