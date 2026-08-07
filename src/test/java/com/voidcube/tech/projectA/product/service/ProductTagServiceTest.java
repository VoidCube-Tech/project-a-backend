package com.voidcube.tech.projectA.product.service;

import com.voidcube.tech.projectA.product.model.ProductTag;
import com.voidcube.tech.projectA.product.repository.ProductTagRepository;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductTagServiceTest {

    @Mock
    private ProductTagRepository productTagRepository;

    @InjectMocks
    private ProductTagService productTagService;

    @Test
    void deveReutilizarTagExistenteDoTenant() {
        Tenant tenant = createTenant(10L);

        ProductTag existingTag = new ProductTag();
        existingTag.setId(5L);
        existingTag.setName("Promoção");
        existingTag.setTenant(tenant);

        when(productTagRepository
                .findByTenant_IdAndNameIgnoreCase(
                        10L,
                        "Promoção"
                ))
                .thenReturn(Optional.of(existingTag));

        Set<ProductTag> result =
                productTagService.findOrCreateForTenant(
                        List.of("Promoção"),
                        tenant
                );

        assertEquals(1, result.size());
        assertSame(existingTag, result.iterator().next());

        verify(productTagRepository, never())
                .save(any(ProductTag.class));
    }

    @Test
    void deveCriarTagQuandoNaoExistirNoTenant() {
        Tenant tenant = createTenant(10L);

        when(productTagRepository
                .findByTenant_IdAndNameIgnoreCase(
                        10L,
                        "Novidade"
                ))
                .thenReturn(Optional.empty());

        when(productTagRepository.save(any(ProductTag.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Set<ProductTag> result =
                productTagService.findOrCreateForTenant(
                        List.of("Novidade"),
                        tenant
                );

        ArgumentCaptor<ProductTag> captor =
                ArgumentCaptor.forClass(ProductTag.class);

        verify(productTagRepository)
                .save(captor.capture());

        ProductTag savedTag = captor.getValue();

        assertEquals("Novidade", savedTag.getName());
        assertSame(tenant, savedTag.getTenant());
        assertEquals(1, result.size());
    }

    @Test
    void deveIgnorarNomesRepetidosNaMesmaRequisicao() {
        Tenant tenant = createTenant(10L);

        when(productTagRepository
                .findByTenant_IdAndNameIgnoreCase(
                        10L,
                        "Promoção"
                ))
                .thenReturn(Optional.empty());

        when(productTagRepository.save(any(ProductTag.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Set<ProductTag> result =
                productTagService.findOrCreateForTenant(
                        List.of(
                                "Promoção",
                                "promoção",
                                "  PROMOÇÃO  "
                        ),
                        tenant
                );

        assertEquals(1, result.size());

        verify(productTagRepository)
                .save(any(ProductTag.class));
    }

    private Tenant createTenant(Long id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);

        return tenant;
    }
}
