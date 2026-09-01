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
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
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
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private static final String COUPON_CODE_UNIQUE_INDEX =
            "ux_promotion_tenant_coupon_code";

    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");

    private final PromotionRepository promotionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;
    private final ProductRepository productRepository;

    @Transactional
    public PromotionResponseDTO create(PromotionRequestDTO request) {
        Tenant tenant = getAuthenticatedTenant();

        validateCommonFields(request);

        Promotion promotion = createPromotionForType(request, tenant.getId(), null);
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

        return promotionRepository.findAllByTenant_Id(tenantId, pageable)
                .map(PromotionResponseDTO::from);
    }

    @Transactional
    public PromotionResponseDTO update(
            Long promotionId,
            PromotionRequestDTO request
    ) {
        Long tenantId = getAuthenticatedTenant().getId();

        validateCommonFields(request);

        Promotion promotion = findPromotion(promotionId, tenantId);

        if (promotion.getPromotionType() != request.promotionType()) {
            throw new InvalidPromotionException(
                    "O tipo da promoção não pode ser alterado"
            );
        }

        Promotion configuredPromotion =
                createPromotionForType(request, tenantId, promotionId);

        promotion.setName(request.name().trim());
        promotion.setActive(request.active());
        copyTypeSpecificFields(promotion, configuredPromotion);

        Promotion savedPromotion = savePromotion(promotion);

        auditLogService.register(
                "PROMOTION_UPDATE",
                "Promotion",
                promotionId.toString()
        );

        return PromotionResponseDTO.from(savedPromotion);
    }

    @Transactional
    public void delete(Long promotionId) {
        Long tenantId = getAuthenticatedTenant().getId();
        Promotion promotion = findPromotion(promotionId, tenantId);

        promotionRepository.delete(promotion);

        auditLogService.register(
                "PROMOTION_DELETE",
                "Promotion",
                promotionId.toString()
        );
    }

    @Transactional
    public boolean associateProduct(Long promotionId, Long productId) {
        Long tenantId = getAuthenticatedTenant().getId();
        Promotion promotion = findPromotion(promotionId, tenantId);
        Product product = findProduct(productId, tenantId);

        if (!promotion.addProduct(product)) {
            return false;
        }

        promotionRepository.saveAndFlush(promotion);
        auditLogService.register(
                "PROMOTION_PRODUCT_ASSOCIATE",
                "PromotionProduct",
                promotionId + ":" + productId
        );

        return true;
    }

    @Transactional
    public boolean disassociateProduct(Long promotionId, Long productId) {
        Long tenantId = getAuthenticatedTenant().getId();
        Promotion promotion = findPromotion(promotionId, tenantId);
        Product product = findProduct(productId, tenantId);

        if (!promotion.removeProduct(product)) {
            return false;
        }

        promotionRepository.saveAndFlush(promotion);
        auditLogService.register(
                "PROMOTION_PRODUCT_DISASSOCIATE",
                "PromotionProduct",
                promotionId + ":" + productId
        );

        return true;
    }

    private Promotion createPromotionForType(
            PromotionRequestDTO request,
            Long tenantId,
            Long excludedPromotionId
    ) {
        return switch (request.promotionType()) {
            case PERCENTAGE -> createPercentagePromotion(request);
            case SCHEDULED -> createScheduledPromotion(request);
            case COUPON -> createCouponPromotion(
                    request,
                    tenantId,
                    excludedPromotionId
            );
        };
    }

    private PercentagePromotion createPercentagePromotion(
            PromotionRequestDTO request
    ) {
        BigDecimal percentage = request.discountPercentage();

        if (percentage == null
                || percentage.signum() <= 0
                || percentage.compareTo(MAX_PERCENTAGE) > 0) {
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
        promotion.setDiscountPercentage(percentage);
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
            Long tenantId,
            Long excludedPromotionId
    ) {
        if (request.couponCode() == null
                || request.couponCode().isBlank()
                || request.discountValue() == null
                || request.usageLimit() == null
                || request.usageLimit() <= 0) {
            throw new InvalidPromotionException(
                    "couponCode, discountValue e usageLimit válidos "
                            + "são obrigatórios para promoções COUPON"
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
                    "couponCode não pode exceder 100 caracteres após a normalização"
            );
        }

        validateUniqueCouponCode(tenantId, excludedPromotionId, couponCode);

        CouponPromotion promotion = new CouponPromotion();
        promotion.setCouponCode(couponCode);
        promotion.setDiscountValue(request.discountValue());
        promotion.setUsageLimit(request.usageLimit());
        return promotion;
    }

    private void validateUniqueCouponCode(
            Long tenantId,
            Long excludedPromotionId,
            String couponCode
    ) {
        boolean exists = excludedPromotionId == null
                ? promotionRepository.existsCouponCodeByTenantId(
                        tenantId,
                        couponCode
                )
                : promotionRepository.existsCouponCodeByTenantIdExcludingPromotionId(
                        tenantId,
                        excludedPromotionId,
                        couponCode
                );

        if (exists) {
            throw new CouponCodeAlreadyExistsException(couponCode);
        }
    }

    private void copyTypeSpecificFields(Promotion target, Promotion source) {
        switch (target.getPromotionType()) {
            case PERCENTAGE -> {
                PercentagePromotion targetPercentage =
                        (PercentagePromotion) target;

                PercentagePromotion sourcePercentage =
                        (PercentagePromotion) source;

                targetPercentage.setDiscountPercentage(
                        sourcePercentage.getDiscountPercentage()
                );
            }

            case SCHEDULED -> {
                ScheduledPromotion targetScheduled = (ScheduledPromotion) target;
                ScheduledPromotion sourceScheduled = (ScheduledPromotion) source;

                targetScheduled.setStartDate(sourceScheduled.getStartDate());
                targetScheduled.setEndDate(sourceScheduled.getEndDate());
                targetScheduled.setDiscountValue(sourceScheduled.getDiscountValue());
            }

            case COUPON -> {
                CouponPromotion targetCoupon = (CouponPromotion) target;
                CouponPromotion sourceCoupon = (CouponPromotion) source;

                targetCoupon.setCouponCode(sourceCoupon.getCouponCode());
                targetCoupon.setDiscountValue(sourceCoupon.getDiscountValue());
                targetCoupon.setUsageLimit(sourceCoupon.getUsageLimit());
            }
        }
    }

    private Promotion savePromotion(Promotion promotion) {
        try {
            return promotionRepository.saveAndFlush(promotion);
        } catch (DataIntegrityViolationException exception) {
            if (promotion instanceof CouponPromotion coupon
                    && isCouponCodeUniqueViolation(exception)) {
                throw new CouponCodeAlreadyExistsException(coupon.getCouponCode());
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

    private Promotion findPromotion(Long promotionId, Long tenantId) {
        return promotionRepository.findByIdAndTenant_Id(promotionId, tenantId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId));
    }

    private Product findProduct(Long productId, Long tenantId) {
        return productRepository.findByIdAndTenant_Id(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private String normalizeCouponCode(String couponCode) {
        return couponCode.trim().toUpperCase(Locale.ROOT);
    }

    private LocalDateTime normalizeDateTime(LocalDateTime dateTime) {
        return dateTime.truncatedTo(ChronoUnit.MICROS);
    }

    private Tenant getAuthenticatedTenant() {
        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getTenant() == null) {
            throw new AccessDeniedException(
                    "O usuário autenticado não possui tenant."
            );
        }

        return user.getTenant();
    }
}