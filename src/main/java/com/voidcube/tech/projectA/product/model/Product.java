package com.voidcube.tech.projectA.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import com.voidcube.tech.projectA.landingPage.model.LandingPage;
import com.voidcube.tech.projectA.promotion.model.Promotion;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)

    @Setter(AccessLevel.NONE)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    
    @Setter(AccessLevel.NONE)
    private List<ProductVariation> variations = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_tag_association", joinColumns = @JoinColumn(name= "product_id"), inverseJoinColumns = @JoinColumn(name= "tag_id"))

    @Setter(AccessLevel.NONE)
    private Set<ProductTag> tags = new LinkedHashSet<>();

    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private Set<LandingPage> landingPages = new LinkedHashSet<>();


    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "products", fetch = FetchType.LAZY)
    private Set<Promotion> promotions = new HashSet<>();


    public void addVariation(ProductVariation variation) {
        variations.add(variation);
        variation.setProduct(this);
    }

    public void removeVariation(ProductVariation variation) {
        boolean removed = variations.remove(variation);
        

        if(!removed) {
            throw new IllegalArgumentException("A variação não pertence a este produto");
        }
        variation.setProduct(null);
    }

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        boolean removedMainImage = image.isMain();
        boolean removed = images.remove(image);

        if(!removed) {
            throw new IllegalArgumentException("A imagem não pertence a este produto");
        }

        image.setProduct(null);

        if(removedMainImage && !images.isEmpty()) {
            images.getFirst().setMain(true);
        }
    }

    public void defineMainImage(ProductImage mainImage) {
        if(!images.contains(mainImage)) {
            throw new IllegalArgumentException("A imagem não pertence a este produto");
        }

        images.forEach(image -> image.setMain(image == mainImage));
    }

    public void addTag(ProductTag tag) {
        tags.add(tag);
    }

    public void removeTag(ProductTag tag) {
        boolean removed = tags.remove(tag);

        if(!removed) {
            throw new IllegalArgumentException("A tag não está associada a este produto.");
        }
    }

    public void replaceTags(Collection<ProductTag> newTags) {
        tags.clear();

        if(newTags != null) {
            tags.addAll(newTags);
        }
    }

    public void replaceVariations(Collection<ProductVariation> newVariations) {
        new ArrayList<>(variations).forEach(this::removeVariation);

        if(newVariations != null) {
            newVariations.forEach(this::addVariation);
        }
    }

    public void markAsDeleted() {
        if(deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public void addLandingPageAssociation(LandingPage landingPage) {
        landingPages.add(landingPage);
    }

    public void removeLandingPageAssociation(LandingPage landingPage) {
        landingPages.remove(landingPage);
    }

    public boolean isAvailable() {
        if(productType == productType.DIGITAL) {
            return true;
        }

        return productType == productType.PHYSICAL
            && stockQuantity != null
            && stockQuantity > 0;
    }

  @PrePersist
    @PreUpdate
    protected void validateProduct() {
        validateStock();
        validatePrice();
        validateMainImage();
    }

    private void validateStock() {
        if (productType == ProductType.PHYSICAL
                && stockQuantity == null) {
            throw new IllegalStateException(
                    "Produto físico precisa possuir quantidade em estoque."
            );
        }

        if (stockQuantity != null && stockQuantity < 0) {
            throw new IllegalStateException(
                    "A quantidade em estoque não pode ser negativa."
            );
        }
    }

    private void validatePrice() {
        if (price != null && price.signum() < 0) {
            throw new IllegalStateException(
                    "O preço não pode ser negativo."
            );
        }
    }

    private void validateMainImage() {
        if (images.isEmpty()) {
            return;
        }

        long mainImageCount = images.stream()
                .filter(image -> image.isMain())
                .count();

        if (mainImageCount > 1) {
            throw new IllegalStateException(
                    "O produto pode possuir somente uma imagem principal."
            );
        }

        if (mainImageCount == 0) {
            images.getFirst().setMain(true);
        }
    }
}
