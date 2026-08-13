package com.voidcube.tech.projectA.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("SCHEDULED")
@NoArgsConstructor
public class ScheduledPromotion extends Promotion {
    
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "scheduled_discount_value", precision = 19, scale = 2)
    private BigDecimal discountValue;

    @PrePersist
    @PreUpdate
    protected void validatePeriod() {
        if (startDate == null || endDate == null) {
            throw new IllegalStateException(
                "O período da promoção não foi configurado"
            );
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalStateException(
                "A data final deve ser posterior à inicial"
            );
        }
    }

    @Override
    public BigDecimal calculatePriceWithDiscount(BigDecimal originalPrice) {
        BigDecimal validPrice = validateOriginalPrice(originalPrice);

        if(!isActive()) {
            return validPrice;
        }

        if(startDate == null || endDate == null) {
            throw new IllegalStateException("O período da promoção não foi configurado");
        }

        if(!endDate.isAfter(startDate)) {
            throw new IllegalStateException("A data final deve ser posterior à inicial");
        }

        LocalDateTime now = LocalDateTime.now();

        boolean hasNotStarted = now.isBefore(startDate);

        boolean hasEnded = now.isAfter(endDate);

        if(hasNotStarted || hasEnded) {
            return validPrice;
        }

        return applyFixedDiscount(validPrice, discountValue);
    }
}
