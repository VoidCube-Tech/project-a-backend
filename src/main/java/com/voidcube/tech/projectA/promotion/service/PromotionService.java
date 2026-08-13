package com.voidcube.tech.projectA.promotion.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.promotion.dto.request.PromotionRequestDTO;
import com.voidcube.tech.projectA.promotion.dto.response.PromotionResponseDTO;
import com.voidcube.tech.projectA.promotion.exception.CouponCodeAlreadyExistsException;
import com.voidcube.tech.projectA.promotion.exception.InvalidPromotionException;
import com.voidcube.tech.projectA.promotion.exception.PromotionNotFoundException;
import com.voidcube.tech.projectA.promotion.model.CouponPromotion;
import com.voidcube.tech.projectA.promotion.model.PercentagePromotion;
import com.voidcube.tech.projectA.promotion.model.Promotion;
import com.voidcube.tech.projectA.promotion.model.PromotionType;
import com.voidcube.tech.projectA.promotion.model.ScheduledPromotion;
import com.voidcube.tech.projectA.promotion.repository.PromotionRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private static final String COUPON_CODE_UNIQUE_INDEX =
        "ux_promotion_tenant_coupon_code";

    private final PromotionRepository promotionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public PromotionResponseDTO create(PromotionRequestDTO request) {
        Tenant tenant = getAuthenticatedTenant();

        validateCommonFields(request);

        Promotion promotion = createPromotionForType(
            request,
            tenant.getId()
        );

        promotion.setName(request.name().trim());
        promotion.setActive(request.active());
        promotion.setTenant(tenant);

        Promotion savedPromotion = savePromotion(promotion);

        auditLogService.register(
            "PROMOTION_CREATE",
            "Promotion",
            savedPromotion.getId().toString()
        );

        return PromotionResponseDTO.from(savedPromotion);
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponseDTO> findAll(Pageable pageable) {
        Long tenantId = getAuthenticatedTenant().getId();

        return promotionRepository
            .findAllByTenant_Id(tenantId, pageable)
            .map(PromotionResponseDTO::from);
    }

    @Transactional
    public void delete(Long promotionId) {
        Long tenantId = getAuthenticatedTenant().getId();

        Promotion promotion = promotionRepository
            .findByIdAndTenant_Id(promotionId, tenantId)
            .orElseThrow(() ->
                new PromotionNotFoundException(promotionId)
            );

        promotionRepository.delete(promotion);

        auditLogService.register(
            "PROMOTION_DELETE",
            "Promotion",
            promotionId.toString()
        );
    }

    private Promotion createPromotionForType(
        PromotionRequestDTO request,
        Long tenantId
    ) {
        return switch (request.promotionType()) {
            case PERCENTAGE -> createPercentagePromotion(request);
            case SCHEDULED -> createScheduledPromotion(request);
            case COUPON -> createCouponPromotion(request, tenantId);
        };
    }

    private PercentagePromotion createPercentagePromotion(
        PromotionRequestDTO request
    ) {
        BigDecimal discountPercentage = request.discountPercentage();

        if (discountPercentage == null
                || discountPercentage.signum() <= 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPromotionException(
                "discountPercentage deve estar entre 0.01 e 100"
            );
        }

        if (request.startDate() != null
                || request.endDate() != null
                || request.discountValue() != null
                || request.couponCode() != null
                || request.usageLimit() != null) {
            throw incompatibleFields(PromotionType.PERCENTAGE);
        }

        PercentagePromotion promotion = new PercentagePromotion();
        promotion.setDiscountPercentage(discountPercentage);

        return promotion;
    }

    private ScheduledPromotion createScheduledPromotion(
        PromotionRequestDTO request
    ) {
        if (request.startDate() == null
                || request.endDate() == null
                || request.discountValue() == null) {
            throw new InvalidPromotionException(
                "startDate, endDate e discountValue são obrigatórios "
                    + "para promoções SCHEDULED"
            );
        }

        LocalDateTime startDate = normalizeDateTime(request.startDate());
        LocalDateTime endDate = normalizeDateTime(request.endDate());

        if (!endDate.isAfter(startDate)) {
            throw new InvalidPromotionException(
                "endDate deve ser posterior a startDate"
            );
        }

        validatePositiveDiscount(request.discountValue());

        if (request.discountPercentage() != null
                || request.couponCode() != null
                || request.usageLimit() != null) {
            throw incompatibleFields(PromotionType.SCHEDULED);
        }

        ScheduledPromotion promotion = new ScheduledPromotion();
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setDiscountValue(request.discountValue());

        return promotion;
    }

    private CouponPromotion createCouponPromotion(
        PromotionRequestDTO request,
        Long tenantId
    ) {
        if (request.couponCode() == null
                || request.couponCode().isBlank()
                || request.discountValue() == null
                || request.usageLimit() == null
                || request.usageLimit() <= 0) {
            throw new InvalidPromotionException(
                "couponCode, discountValue e usageLimit válidos são "
                    + "obrigatórios para promoções COUPON"
            );
        }

        validatePositiveDiscount(request.discountValue());

        if (request.discountPercentage() != null
                || request.startDate() != null
                || request.endDate() != null) {
            throw incompatibleFields(PromotionType.COUPON);
        }

        String couponCode = normalizeCouponCode(request.couponCode());

        if (couponCode.length() > 100) {
            throw new InvalidPromotionException(
                "couponCode não pode exceder 100 caracteres "
                    + "após a normalização"
            );
        }

        if (promotionRepository.existsCouponCodeByTenantId(
                tenantId,
                couponCode
        )) {
            throw new CouponCodeAlreadyExistsException(couponCode);
        }

        CouponPromotion promotion = new CouponPromotion();
        promotion.setCouponCode(couponCode);
        promotion.setDiscountValue(request.discountValue());
        promotion.setUsageLimit(request.usageLimit());

        return promotion;
    }

    private Promotion savePromotion(Promotion promotion) {
        try {
            return promotionRepository.saveAndFlush(promotion);
        } catch (DataIntegrityViolationException exception) {
            if (promotion instanceof CouponPromotion couponPromotion
                    && isCouponCodeUniqueViolation(exception)) {
                throw new CouponCodeAlreadyExistsException(
                    couponPromotion.getCouponCode()
                );
            }

            throw exception;
        }
    }

    private boolean isCouponCodeUniqueViolation(Throwable exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation) {
                return COUPON_CODE_UNIQUE_INDEX.equals(
                    violation.getConstraintName()
                );
            }

            cause = cause.getCause();
        }

        return false;
    }

    private void validateCommonFields(PromotionRequestDTO request) {
        if (request == null) {
            throw new InvalidPromotionException(
                "Os dados da promoção são obrigatórios"
            );
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new InvalidPromotionException(
                "O nome da promoção é obrigatório"
            );
        }

        if (request.active() == null) {
            throw new InvalidPromotionException(
                "O estado da promoção é obrigatório"
            );
        }

        if (request.promotionType() == null) {
            throw new InvalidPromotionException(
                "O tipo da promoção é obrigatório"
            );
        }

    }

    private void validatePositiveDiscount(BigDecimal discountValue) {
        if (discountValue == null || discountValue.signum() <= 0) {
            throw new InvalidPromotionException(
                "discountValue deve ser maior que zero"
            );
        }
    }

    private InvalidPromotionException incompatibleFields(
        PromotionType promotionType
    ) {
        return new InvalidPromotionException(
            "Foram informados campos incompatíveis com o tipo "
                + promotionType
        );
    }

    private String normalizeCouponCode(String couponCode) {
        return couponCode
            .trim()
            .toUpperCase(Locale.ROOT);
    }

    private LocalDateTime normalizeDateTime(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MICROS);
    }

    private Tenant getAuthenticatedTenant() {
        User authenticatedUser = authenticatedUserProvider
            .getAuthenticatedUser();

        if (authenticatedUser.getTenant() == null) {
            throw new AccessDeniedException(
                "O usuário autenticado não possui Tenant."
            );
        }

        return authenticatedUser.getTenant();
    }
}
