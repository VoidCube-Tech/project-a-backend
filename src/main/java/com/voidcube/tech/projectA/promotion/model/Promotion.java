package com.voidcube.tech.projectA.promotion.model;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.tenant.model.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "promotion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "promotion_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false, insertable = false, updatable = false)
    private PromotionType promotionType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_promotion", joinColumns = @JoinColumn(name = "promotion_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<Product> products = new LinkedHashSet<>();

    protected Promotion() {}

    public abstract BigDecimal calculatePriceWithDiscount(BigDecimal originalPrice);

    protected BigDecimal validateOriginalPrice(BigDecimal originalPrice) {
        if (originalPrice == null) {
            throw new IllegalArgumentException("O preço original não pode ser nulo");
        }

        if (originalPrice.signum() < 0) {
            throw new IllegalArgumentException("O preço original não pode ser negativo ou abaixo de zero");
        }

        return originalPrice;
    }

    protected BigDecimal applyFixedDiscount(BigDecimal originalPrice, BigDecimal discountValue) {
        BigDecimal validPrice = validateOriginalPrice(originalPrice);

        if(discountValue == null) {
            throw new IllegalArgumentException("Desconto não foi configurado/sem desconto");
        }

        if(discountValue.signum() <= 0) {
            throw new IllegalArgumentException("Desconto deve ser maior que zero");
        }

        return validPrice
            .subtract(discountValue)
            .max(BigDecimal.ZERO);
    }

    public boolean addProduct(Product product) {
        Objects.requireNonNull(product, "O produto não pode ser nulo");

        boolean added = products.add(product);

        if(added) {
            product.getPromotions().add(this);
        }

        return added;
    }

    public boolean removeProduct(Product product) {
        Objects.requireNonNull(product, "O produto não pode ser nulo");

        boolean removed = products.remove(product);

        if(removed) {
            product.getPromotions().remove(this);
        }
        return removed;
    }
}
