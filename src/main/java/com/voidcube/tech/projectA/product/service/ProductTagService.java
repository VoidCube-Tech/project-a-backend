package com.voidcube.tech.projectA.product.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.product.model.ProductTag;
import com.voidcube.tech.projectA.product.repository.ProductTagRepository;
import com.voidcube.tech.projectA.tenant.model.Tenant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductTagService {
    
    private final ProductTagRepository productTagRepository;

    @Transactional
    public Set<ProductTag> findOrCreateForTenant(Collection<String> tagNames, Tenant tenant) {
        if(tenant ==  null || tenant.getId() == null) {
            throw new IllegalArgumentException("O tenant precisa estar salvo antes de criar suas tags");
        }

        Set<ProductTag> resolvedTags = new LinkedHashSet<>();
        Set<String> processedNames = new HashSet<>();

        if(tagNames == null) {
            return resolvedTags;
        }

        for(String rawName : tagNames) {
            if(rawName == null || rawName.isBlank()) {
                continue;
            }
            String tagName = rawName.trim();
            String normalizedName = tagName.toLowerCase(Locale.ROOT);

            if(!processedNames.add(normalizedName)) {
                continue;
            }

            ProductTag tag = productTagRepository.findByTenant_IdAndNameIgnoreCase(tenant.getId(), tagName)
                .orElseGet(() -> createTag(tagName, tenant));

                resolvedTags.add(tag);
        }
        return resolvedTags;
    }

    private ProductTag createTag(String name, Tenant tenant) {
        ProductTag tag = new ProductTag();

        tag.setName(name);
        tag.setTenant(tenant);

        return productTagRepository.save(tag);
    }
}
