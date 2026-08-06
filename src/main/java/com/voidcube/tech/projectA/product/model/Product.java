package com.voidcube.tech.projectA.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.voidcube.tech.projectA.tenant.model.Tenant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product")
@SQLDelete(sql= "UPDATE product SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product {


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    
    @Setter(AccessLevel.NONE)
    private List<ProductVariation> variations = new ArrayList<>();

    public void addVariation(ProductVariation variation) {
        variations.add(variation);
        variation.setProduct(this);
    }

    public void removeVariation(ProductVariation variation) {
        variations.remove(variation);
        variation.setProduct(null);
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_type")
    private ProductType productType;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description")
    private String description;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @PrePersist
    @PreUpdate

    protected void validateProduct() {
        if(productType == ProductType.PHYSICAL && stockQuantity == null) {
            throw new IllegalStateException("Produto físico precisa possuir quantidade em estoque. ");
        }

        if (stockQuantity != null && stockQuantity < 0 ) {
            throw new IllegalStateException("A quantidade em estoque não pode ser negativo");
        }

        if(price != null && price.signum() < 0) {
            throw new IllegalStateException("O preço não pode ser negativo");
        }
    }
}
