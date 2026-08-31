package com.voidcube.tech.projectA.sale.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.inventory.repository.InventoryMovementRepository;
import com.voidcube.tech.projectA.product.model.Product;
import com.voidcube.tech.projectA.product.model.ProductType;
import com.voidcube.tech.projectA.product.model.ProductVariation;
import com.voidcube.tech.projectA.product.repository.ProductRepository;
import com.voidcube.tech.projectA.product.repository.ProductVariationRepository;
import com.voidcube.tech.projectA.sale.dto.request.SaleItemRequestDTO;
import com.voidcube.tech.projectA.sale.dto.request.SaleRequestDTO;
import com.voidcube.tech.projectA.sale.dto.response.SaleResponseDTO;
import com.voidcube.tech.projectA.sale.exception.InsufficientStockException;
import com.voidcube.tech.projectA.sale.exception.SaleAlreadyCancelledException;
import com.voidcube.tech.projectA.sale.model.Sale;
import com.voidcube.tech.projectA.sale.model.SaleItem;
import com.voidcube.tech.projectA.sale.model.SaleStatus;
import com.voidcube.tech.projectA.sale.repository.SaleRepository;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private SaleService saleService;

    @Test
    void deveRegistrarVendasFisicaEBaixarEstoque() {
        User admin = authenticateAdmin();
        Product product = createProduct(20L, ProductType.PHYSICAL);

        when(productRepository.findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));
        when(productRepository.decrementStock(20L, 10L, 2)).thenReturn(1);
        mockSavedSale(100L);

        SaleResponseDTO response = saleService.create(
                request(20L, null, 2, "75.00")
        );

        assertEquals(100L, response.id());
        assertEquals(new BigDecimal("150.00"), response.totalAmount());
        assertEquals("Maria", response.customerName());
        assertEquals("91999999999", response.customerPhone());
        assertEquals(admin.getId(), response.registeredByUserId());

        verify(productRepository).decrementStock(20L, 10L, 2);
        verifyNoInteractions(productVariationRepository);
        verify(inventoryMovementRepository).saveAll(anyList());
        verify(auditLogService).register("SALE_CREATE", "Sale", "100");
    }

    @Test
    void deveBaixarEstoqueTotalEDaVariacao() {
        authenticateAdmin();
        Product product = createProduct(20L, ProductType.PHYSICAL);
        ProductVariation variation = createVariation(30L);
        product.addVariation(variation);

        when(productRepository.findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));
        when(productRepository.decrementStock(20L, 10L, 2)).thenReturn(1);
        when(productVariationRepository.decrementStock(30L, 20L, 10L, 2))
                .thenReturn(1);
        mockSavedSale(100L);

        SaleResponseDTO response = saleService.create(
                request(20L, 30L, 2, "75.00")
        );

        assertEquals(30L, response.items().getFirst().variationId());
        assertEquals(
                "Tamanho: M",
                response.items().getFirst().variationDescription()
        );

        verify(productRepository).decrementStock(20L, 10L, 2);
        verify(productVariationRepository).decrementStock(30L, 20L, 10L, 2);
    }

    @Test
    void deveRejeitarVendaSemEstoque() {
        authenticateAdmin();
        Product product = createProduct(20L, ProductType.PHYSICAL);

        when(productRepository.findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));
        when(productRepository.decrementStock(20L, 10L, 20)).thenReturn(0);

        assertThrows(
                InsufficientStockException.class,
                () -> saleService.create(request(20L, null, 20, "75.00"))
        );

        verify(saleRepository, never()).saveAndFlush(any(Sale.class));
        verifyNoInteractions(inventoryMovementRepository);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void naoDeveBaixarEstoqueDeProdutoDigital() {
        authenticateAdmin();
        Product product = createProduct(20L, ProductType.DIGITAL);

        when(productRepository.findByIdAndTenant_Id(20L, 10L))
                .thenReturn(Optional.of(product));
        mockSavedSale(100L);

        SaleResponseDTO response = saleService.create(
                request(20L, null, 1, "50.00")
        );

        assertEquals(new BigDecimal("50.00"), response.totalAmount());

        verify(productRepository, never())
                .decrementStock(anyLong(), anyLong(), anyInt());
        verifyNoInteractions(productVariationRepository);
        verify(inventoryMovementRepository).saveAll(List.of());
    }

    @Test
    void deveCancelarVendaEDevolverEstoque() {
        User admin = authenticateAdmin();
        Sale sale = createConfirmedSale(admin.getTenant());

        when(saleRepository.findByIdAndTenantIdForUpdate(100L, 10L))
                .thenReturn(Optional.of(sale));
        when(productRepository.restoreStock(20L, 10L, 2)).thenReturn(1);
        when(saleRepository.saveAndFlush(sale)).thenReturn(sale);

        SaleResponseDTO response = saleService.cancel(100L);

        assertEquals(SaleStatus.CANCELLED, response.status());
        assertEquals(admin.getId(), response.cancelledByUserId());

        verify(productRepository).restoreStock(20L, 10L, 2);
        verify(inventoryMovementRepository).saveAll(anyList());
        verify(auditLogService).register("SALE_CANCEL", "Sale", "100");
    }

    @Test
    void naoDeveCancelarVendaDuasVezes() {
        User admin = authenticateAdmin();
        Sale sale = createConfirmedSale(admin.getTenant());
        sale.cancel(admin.getId());

        when(saleRepository.findByIdAndTenantIdForUpdate(100L, 10L))
                .thenReturn(Optional.of(sale));

        assertThrows(
                SaleAlreadyCancelledException.class,
                () -> saleService.cancel(100L)
        );

        verify(productRepository, never())
                .restoreStock(anyLong(), anyLong(), anyInt());
        verifyNoInteractions(productVariationRepository);
        verifyNoInteractions(inventoryMovementRepository);
        verifyNoInteractions(auditLogService);
    }

    private User authenticateAdmin() {
        Tenant tenant = new Tenant();
        tenant.setId(10L);

        User admin = new User();
        admin.setId(5L);
        admin.setEmail("admin@teste.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setTenant(tenant);

        when(authenticatedUserProvider.getAuthenticatedUser())
                .thenReturn(admin);

        return admin;
    }

    private Product createProduct(Long id, ProductType type) {
        Product product = new Product();
        product.setId(id);
        product.setName("Produto");
        product.setProductType(type);
        product.setPrice(new BigDecimal("100.00"));
        product.setStockQuantity(type == ProductType.PHYSICAL ? 10 : null);
        return product;
    }

    private ProductVariation createVariation(Long id) {
        ProductVariation variation = new ProductVariation();
        variation.setId(id);
        variation.setVariationName("Tamanho");
        variation.setVariationValue("M");
        variation.setStockQuantity(5);
        return variation;
    }

    private SaleRequestDTO request(
            Long productId,
            Long variationId,
            Integer quantity,
            String unitPrice
    ) {
        SaleItemRequestDTO item = new SaleItemRequestDTO(
                productId,
                variationId,
                quantity,
                new BigDecimal(unitPrice)
        );

        return new SaleRequestDTO(
                " Maria ",
                "(91) 99999-9999",
                List.of(item)
        );
    }

    private void mockSavedSale(Long saleId) {
        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> {
                    Sale sale = invocation.getArgument(0);
                    sale.setId(saleId);
                    return sale;
                });
    }

    private Sale createConfirmedSale(Tenant tenant) {
        Sale sale = new Sale();
        sale.setId(100L);
        sale.setTenant(tenant);
        sale.setStatus(SaleStatus.CONFIRMED);
        sale.setTotalAmount(new BigDecimal("150.00"));
        sale.setRegisteredByUserId(5L);

        SaleItem item = new SaleItem();
        item.setProductId(20L);
        item.setProductName("Produto");
        item.setProductType(ProductType.PHYSICAL);
        item.setUnitPrice(new BigDecimal("75.00"));
        item.setQuantity(2);
        item.setSubtotal(new BigDecimal("150.00"));
        sale.addItem(item);

        return sale;
    }
}