package com.voidcube.tech.projectA.shared.validation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.landingpage.dto.request.LandingPageRequestDTO;
import com.voidcube.tech.projectA.product.dto.request.ProductRequestDTO;
import com.voidcube.tech.projectA.product.dto.request.ProductVariationRequestDTO;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.promotion.dto.request.PromotionRequestDTO;
import com.voidcube.tech.projectA.promotion.model.PromotionType;
import com.voidcube.tech.projectA.tenant.dto.request.UpdateTenantPlanRequest;
import com.voidcube.tech.projectA.user.dto.request.LoginRequestDTO;
import com.voidcube.tech.projectA.user.dto.request.RegisterRequestDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory =
                Validation
                        .buildDefaultValidatorFactory();

        validator =
                validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectNonPositiveAnalyticsIds() {
        AnalyticsEventRequestDTO request =
                new AnalyticsEventRequestDTO(
                        0L,
                        -1L,
                        EventType.VIEW
                );

        Set<ConstraintViolation<AnalyticsEventRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains(
                        "landingPageId",
                        "productId"
                );
    }

    @Test
    void shouldRejectInvalidLandingPageDomain() {
        LandingPageRequestDTO request =
                new LandingPageRequestDTO(
                        "Minha loja",
                        "-dominio-invalido-",
                        null
                );

        Set<ConstraintViolation<LandingPageRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains("domainUrl");
    }

    @Test
    void shouldValidateNestedProductVariation() {
        ProductVariationRequestDTO variation =
                new ProductVariationRequestDTO(
                        "",
                        "",
                        -1
                );

        ProductRequestDTO request =
                new ProductRequestDTO(
                        "Camiseta",
                        new BigDecimal("50.00"),
                        null,
                        ProductType.PHYSICAL,
                        10,
                        List.of(),
                        List.of(variation)
                );

        Set<ConstraintViolation<ProductRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains(
                        "variations[0].variationName",
                        "variations[0].variationValue",
                        "variations[0].stockQuantity"
                );
    }

    @Test
    void shouldRejectPercentageAboveOneHundred() {
        PromotionRequestDTO request =
                new PromotionRequestDTO(
                        "Promoção",
                        true,
                        PromotionType.values()[0],
                        new BigDecimal("101.00"),
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Set<ConstraintViolation<PromotionRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains("discountPercentage");
    }

    @Test
    void shouldRejectNonPositivePlanId() {
        UpdateTenantPlanRequest request =
                new UpdateTenantPlanRequest(0L);

        Set<ConstraintViolation<UpdateTenantPlanRequest>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains("planId");
    }

    @Test
    void shouldRejectWeakRegistrationPassword() {
        RegisterRequestDTO request =
                new RegisterRequestDTO(
                        "Minha empresa",
                        "usuario@email.com",
                        "senha"
                );

        Set<ConstraintViolation<RegisterRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation
                                .getPropertyPath()
                                .toString()
                )
                .contains("password");
    }

    @Test
    void shouldNotApplyRegistrationPolicyToLogin() {
        LoginRequestDTO request =
                new LoginRequestDTO(
                        "usuario@email.com",
                        "senha-antiga"
                );

        Set<ConstraintViolation<LoginRequestDTO>>
                violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
