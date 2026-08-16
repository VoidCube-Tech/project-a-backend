package com.voidcube.tech.projectA.landingpage.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.tenant.model.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "landing_page")
public class LandingPage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "domain_url", unique = true)
    private String domainUrl;

    @Column(nullable = false, name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_landing_page",
        joinColumns = @JoinColumn(name = "landing_page_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Setter(AccessLevel.NONE)
    private Set<Product> products = new LinkedHashSet<>();


    public boolean addProduct(Product product) {
        boolean added = products.add(product);

        if(added) {
            product.addLandingPageAssociation(this);
        }

        return added;
    }

    public boolean removeProduct(Product product) {
        boolean removed = products.remove(product);

        if(removed) {
            product.removeLandingPageAssociation(this);
        }
        return removed;
    }
}
