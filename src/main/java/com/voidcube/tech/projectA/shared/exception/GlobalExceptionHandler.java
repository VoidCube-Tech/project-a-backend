package com.voidcube.tech.projectA.shared.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.voidcube.tech.projectA.inventory.exception.InventoryConsistencyException;
import com.voidcube.tech.projectA.export.exception.InvalidExportFormatException;
import com.voidcube.tech.projectA.promotion.exception.CouponCodeAlreadyExistsException;
import com.voidcube.tech.projectA.promotion.exception.InvalidPromotionException;
import com.voidcube.tech.projectA.promotion.exception.PromotionNotFoundException;
import com.voidcube.tech.projectA.sale.exception.InsufficientStockException;
import com.voidcube.tech.projectA.sale.exception.InvalidSaleException;
import com.voidcube.tech.projectA.sale.exception.SaleAlreadyCancelledException;
import com.voidcube.tech.projectA.sale.exception.SaleNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas.",
                request
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabled(
            DisabledException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Conta não verificada. Confira seu e-mail.",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            CouponCodeAlreadyExistsException.class,
            DomainUrlAlreadyException.class,
            InsufficientStockException.class,
            SaleAlreadyCancelledException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            InvalidTokenException.class,
            TokenExpiredException.class,
            InvalidProductException.class,
            InvalidPromotionException.class,
            InvalidImageException.class,
            InvalidPageException.class,
            InvalidExportFormatException.class,
            InvalidSaleException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler({
            ProductNotFoundException.class,
            PromotionNotFoundException.class,
            ProductImageNotFoundException.class,
            LandingPageNotFoundException.class,
            TenantNotFoundException.class,
            PlanNotFoundException.class,
            SaleNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(WhatsappNotConfiguredException.class)
    public ResponseEntity<ApiErrorResponse>
            handleWhatsappNotConfigured(
                    WhatsappNotConfiguredException exception,
                    HttpServletRequest request
            ) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InventoryConsistencyException.class)
        public ResponseEntity<ApiErrorResponse> handleInventoryConsistency(
        InventoryConsistencyException exception,
        HttpServletRequest request)       
        {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(),
            request
    );
}

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleImageStorage(
            ImageStorageException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível processar o arquivo da imagem.",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONTENT_TOO_LARGE,
                "A imagem ultrapassa o limite máximo permitido de 5 MB.",
                request
        );
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskRejected(
            TaskRejectedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "O serviço de analytics está temporariamente "
                        + "sobrecarregado. Tente novamente.",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse>
            handleRequestBodyValidation(
                    MethodArgumentNotValidException exception,
                    HttpServletRequest request
            ) {
        List<FieldValidationErrorResponse> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .distinct()
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos são inválidos.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<FieldValidationErrorResponse> fieldErrors = exception
                .getParameterValidationResults()
                .stream()
                .flatMap(result -> result
                        .getResolvableErrors()
                        .stream()
                        .map(error -> new FieldValidationErrorResponse(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage()
                        ))
                )
                .distinct()
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Um ou mais parâmetros são inválidos.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse>
            handleConstraintViolation(
                    ConstraintViolationException exception,
                    HttpServletRequest request
            ) {
        List<FieldValidationErrorResponse> fieldErrors = exception
                .getConstraintViolations()
                .stream()
                .map(violation -> new FieldValidationErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .distinct()
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Um ou mais parâmetros são inválidos.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "JSON inválido. Verifique os valores e os tipos "
                        + "dos campos informados.",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return buildResponse(status, message, request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldValidationErrorResponse> fieldErrors
    ) {
        String safeMessage = message == null || message.isBlank()
                ? status.getReasonPhrase()
                : message;

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                safeMessage,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}