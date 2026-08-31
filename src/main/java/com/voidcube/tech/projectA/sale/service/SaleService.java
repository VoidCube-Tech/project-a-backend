package com.voidcube.tech.projectA.sale.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.audit.service.AuditLogService;
import com.voidcube.tech.projectA.inventory.exception.InventoryConsistencyException;
import com.voidcube.tech.projectA.inventory.model.InventoryMovement;
import com.voidcube.tech.projectA.inventory.model.InventoryMovementType;
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
import com.voidcube.tech.projectA.sale.exception.InvalidSaleException;
import com.voidcube.tech.projectA.sale.exception.SaleAlreadyCancelledException;
import com.voidcube.tech.projectA.sale.exception.SaleNotFoundException;
import com.voidcube.tech.projectA.sale.model.Sale;
import com.voidcube.tech.projectA.sale.model.SaleItem;
import com.voidcube.tech.projectA.sale.model.SaleStatus;
import com.voidcube.tech.projectA.sale.repository.SaleRepository;
import com.voidcube.tech.projectA.shared.exception.ProductNotFoundException;
import com.voidcube.tech.projectA.shared.security.AuthenticatedUserProvider;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public SaleResponseDTO create(SaleRequestDTO request) {
        User user = authenticatedUserProvider.getAuthenticatedUser();
        Tenant tenant = requireTenant(user);

        validateNoDuplicateItems(request.items());

        Sale sale = createSale(request, user, tenant);
        List<InventoryMovement> movements = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);

        for (SaleItemRequestDTO requestItem : request.items()) {
            Product product = findProduct(requestItem.productId(), tenant.getId());
            ProductVariation variation = resolveVariation(product, requestItem.variationId());

            decrementStock(product, variation, requestItem.quantity(), tenant.getId());

            SaleItem item = createItem(requestItem, product, variation);
            sale.addItem(item);
            total = total.add(item.getSubtotal());

            if (product.getProductType() == ProductType.PHYSICAL) {
                movements.add(createMovement(
                        sale,
                        tenant,
                        item,
                        user.getId(),
                        InventoryMovementType.SALE
                ));
            }
        }

        sale.setTotalAmount(total);
        Sale savedSale = saleRepository.saveAndFlush(sale);

        inventoryMovementRepository.saveAll(movements);
        auditLogService.register(
                "SALE_CREATE",
                "Sale",
                savedSale.getId().toString()
        );

        return SaleResponseDTO.from(savedSale);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDTO> findAll(Pageable pageable) {
        Long tenantId = authenticatedUserProvider.getRequiredTenantId();

        return saleRepository.findAllByTenant_Id(tenantId, pageable)
                .map(SaleResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public SaleResponseDTO findById(Long saleId) {
        Long tenantId = authenticatedUserProvider.getRequiredTenantId();

        Sale sale = saleRepository.findByIdAndTenant_Id(saleId, tenantId)
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        return SaleResponseDTO.from(sale);
    }

    @Transactional
    public SaleResponseDTO cancel(Long saleId) {
        User user = authenticatedUserProvider.getAuthenticatedUser();
        Tenant tenant = requireTenant(user);

        Sale sale = saleRepository
                .findByIdAndTenantIdForUpdate(saleId, tenant.getId())
                .orElseThrow(() -> new SaleNotFoundException(saleId));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new SaleAlreadyCancelledException(saleId);
        }

        List<InventoryMovement> movements = new ArrayList<>();

        for (SaleItem item : sale.getItems()) {
            if (item.getProductType() != ProductType.PHYSICAL) {
                continue;
            }

            restoreStock(item, tenant.getId(), saleId);

            movements.add(createMovement(
                    sale,
                    tenant,
                    item,
                    user.getId(),
                    InventoryMovementType.SALE_CANCELLATION
            ));
        }

        sale.cancel(user.getId());
        Sale cancelledSale = saleRepository.saveAndFlush(sale);

        inventoryMovementRepository.saveAll(movements);
        auditLogService.register(
                "SALE_CANCEL",
                "Sale",
                saleId.toString()
        );

        return SaleResponseDTO.from(cancelledSale);
    }

    private Sale createSale(SaleRequestDTO request, User user, Tenant tenant) {
        Sale sale = new Sale();
        sale.setTenant(tenant);
        sale.setCustomerName(normalizeCustomerName(request.customerName()));
        sale.setCustomerPhone(normalizeCustomerPhone(request.customerPhone()));
        sale.setStatus(SaleStatus.CONFIRMED);
        sale.setRegisteredByUserId(user.getId());
        return sale;
    }

    private SaleItem createItem(
            SaleItemRequestDTO request,
            Product product,
            ProductVariation variation
    ) {
        BigDecimal unitPrice = request.unitPrice()
                .setScale(2, RoundingMode.UNNECESSARY);

        SaleItem item = new SaleItem();
        item.setProductId(product.getId());
        item.setVariationId(variation == null ? null : variation.getId());
        item.setProductName(product.getName());
        item.setVariationDescription(toVariationDescription(variation));
        item.setProductType(product.getProductType());
        item.setUnitPrice(unitPrice);
        item.setQuantity(request.quantity());
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private InventoryMovement createMovement(
            Sale sale,
            Tenant tenant,
            SaleItem item,
            Long userId,
            InventoryMovementType type
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setTenant(tenant);
        movement.setSale(sale);
        movement.setProductId(item.getProductId());
        movement.setVariationId(item.getVariationId());
        movement.setMovementType(type);
        movement.setQuantity(item.getQuantity());
        movement.setPerformedByUserId(userId);
        return movement;
    }

    private void decrementStock(
            Product product,
            ProductVariation variation,
            Integer quantity,
            Long tenantId
    ) {
        if (product.getProductType() == ProductType.DIGITAL) {
            return;
        }

        int updatedProducts = productRepository.decrementStock(
                product.getId(),
                tenantId,
                quantity
        );

        if (updatedProducts == 0) {
            throw new InsufficientStockException(product.getId(), null);
        }

        if (variation == null) {
            return;
        }

        int updatedVariations = productVariationRepository.decrementStock(
                variation.getId(),
                product.getId(),
                tenantId,
                quantity
        );

        if (updatedVariations == 0) {
            throw new InsufficientStockException(
                    product.getId(),
                    variation.getId()
            );
        }
    }

    private void restoreStock(SaleItem item, Long tenantId, Long saleId) {
        int restoredProducts = productRepository.restoreStock(
                item.getProductId(),
                tenantId,
                item.getQuantity()
        );

        if (restoredProducts == 0) {
            throw new InventoryConsistencyException(saleId);
        }

        if (item.getVariationId() == null) {
            return;
        }

        int restoredVariations = productVariationRepository.restoreStock(
                item.getVariationId(),
                item.getProductId(),
                tenantId,
                item.getQuantity()
        );

        if (restoredVariations == 0) {
            throw new InventoryConsistencyException(saleId);
        }
    }

    private ProductVariation resolveVariation(
            Product product,
            Long variationId
    ) {
        boolean hasVariations = !product.getVariations().isEmpty();

        if (hasVariations && variationId == null) {
            throw new InvalidSaleException(
                    "O produto " + product.getId()
                    + " exige uma variação."
            );
        }

        if (!hasVariations && variationId != null) {
            throw new InvalidSaleException(
                    "O produto " + product.getId()
                    + " não possui variações."
            );
        }

        if (variationId == null) {
            return null;
        }

        return product.getVariations()
                .stream()
                .filter(variation -> variationId.equals(variation.getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidSaleException(
                        "A variação " + variationId
                        + " não pertence ao produto "
                        + product.getId() + "."
                ));
    }

    private Product findProduct(Long productId, Long tenantId) {
        return productRepository.findByIdAndTenant_Id(productId, tenantId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void validateNoDuplicateItems(List<SaleItemRequestDTO> items) {
        Set<ItemKey> keys = new HashSet<>();

        for (SaleItemRequestDTO item : items) {
            ItemKey key = new ItemKey(item.productId(), item.variationId());

            if (!keys.add(key)) {
                throw new InvalidSaleException(
                        "A venda possui itens duplicados para o produto "
                        + item.productId() + "."
                );
            }
        }
    }

    private Tenant requireTenant(User user) {
        if (user.getTenant() == null) {
            throw new AccessDeniedException(
                    "O usuário autenticado não possui tenant."
            );
        }

        return user.getTenant();
    }

    private String normalizeCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return null;
        }

        return customerName.trim();
    }

    private String normalizeCustomerPhone(String customerPhone) {
        if (customerPhone == null || customerPhone.isBlank()) {
            return null;
        }

        if (!customerPhone.matches("^[0-9() +\\-]*$")) {
            throw new InvalidSaleException(
                    "O telefone do cliente possui caracteres inválidos."
            );
        }

        String digitsOnly = customerPhone.replaceAll("\\D", "");

        if (digitsOnly.length() < 8 || digitsOnly.length() > 15) {
            throw new InvalidSaleException(
                    "O telefone do cliente deve possuir entre 8 e 15 dígitos."
            );
        }

        return digitsOnly;
    }

    private String toVariationDescription(ProductVariation variation) {
        if (variation == null) {
            return null;
        }

        return variation.getVariationName()
                + ": "
                + variation.getVariationValue();
    }

    private record ItemKey(Long productId, Long variationId) {
    }
}