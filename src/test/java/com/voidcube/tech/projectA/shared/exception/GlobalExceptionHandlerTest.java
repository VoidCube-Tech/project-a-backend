package com.voidcube.tech.projectA.shared.exception;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        request = new MockHttpServletRequest();
        request.setRequestURI(
                "/api/v1/auth/register"
        );
    }

    @Test
    void shouldReturnStandardValidationResponse() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(
                        new Object(),
                        "request"
                );

        bindingResult.addError(
                new FieldError(
                        "request",
                        "email",
                        "O e-mail informado é inválido"
                )
        );

        MethodParameter methodParameter =
                mock(MethodParameter.class);

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        methodParameter,
                        bindingResult
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleRequestBodyValidation(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(400);

        assertThat(response.getBody().error())
                .isEqualTo("Bad Request");

        assertThat(response.getBody().path())
                .isEqualTo(
                        "/api/v1/auth/register"
                );

        assertThat(response.getBody().fieldErrors())
                .hasSize(1);

        assertThat(
                response
                        .getBody()
                        .fieldErrors()
                        .getFirst()
                        .field()
        ).isEqualTo("email");

        assertThat(
                response
                        .getBody()
                        .fieldErrors()
                        .getFirst()
                        .message()
        ).isEqualTo(
                "O e-mail informado é inválido"
        );
    }

    @Test
    void shouldReturnStandardNotFoundResponse() {
        ProductNotFoundException exception =
                mock(ProductNotFoundException.class);

        when(exception.getMessage())
                .thenReturn(
                        "Produto não encontrado"
                );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotFound(
                        exception,
                        request
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .isNotNull();

        assertThat(response.getBody().status())
                .isEqualTo(404);

        assertThat(response.getBody().error())
                .isEqualTo("Not Found");

        assertThat(response.getBody().message())
                .isEqualTo(
                        "Produto não encontrado"
                );

        assertThat(response.getBody().fieldErrors())
                .isEmpty();
    }
}