package com.voidcube.tech.projectA.promotion.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

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
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PromotionService promotionService;

    private Tenant tenant;
    private User admin;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(10L);

        admin = new User();
        admin.setId(5L);
        admin.setEmail("admin@teste.com");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setTenant(tenant);

        when(
                authenticatedUserProvider
                        .getAuthenticatedUser()
        ).thenReturn(admin);
    }

    @Test
    void deveCriarPromocaoPercentualParaOTenantAutenticado() {
        configureSave();

        PromotionResponseDTO response =
                promotionService.create(
                        percentageRequest()
                );

        ArgumentCaptor<Promotion> captor =
                ArgumentCaptor.forClass(
                        Promotion.class
                );

        verify(
                promotionRepository
        ).saveAndFlush(captor.capture());

        PercentagePromotion saved =
                assertInstanceOf(
                        PercentagePromotion.class,
                        captor.getValue()
                );

        assertSame(tenant, saved.getTenant());
        assertEquals(
                "Promo percentual",
                saved.getName()
        );
        assertEquals(
                new BigDecimal("15.00"),
                saved.getDiscountPercentage()
        );
        assertEquals(
                PromotionType.PERCENTAGE,
                response.promotionType()
        );

        verify(auditLogService).register(
                "PROMOTION_CREATE",
                "Promotion",
                "100"
        );
    }

    @Test
    void deveCriarPromocaoAgendadaComPeriodoValido() {
        configureSave();

        LocalDateTime startDate =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        8,
                        0
                );

        LocalDateTime endDate =
                startDate.plusDays(2);

        PromotionResponseDTO response =
                promotionService.create(
                        scheduledRequest(
                                startDate,
                                endDate
                        )
                );

        ArgumentCaptor<Promotion> captor =
                ArgumentCaptor.forClass(
                        Promotion.class
                );

        verify(
                promotionRepository
        ).saveAndFlush(captor.capture());

        ScheduledPromotion saved =
                assertInstanceOf(
                        ScheduledPromotion.class,
                        captor.getValue()
                );

        assertEquals(
                startDate,
                saved.getStartDate()
        );
        assertEquals(
                endDate,
                saved.getEndDate()
        );
        assertEquals(
                new BigDecimal("25.00"),
                saved.getDiscountValue()
        );
        assertEquals(
                PromotionType.SCHEDULED,
                response.promotionType()
        );
    }

    @Test
    void deveRejeitarDataFinalIgualOuAnteriorAInicial() {
        LocalDateTime startDate =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        8,
                        0
                );

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(
                        scheduledRequest(
                                startDate,
                                startDate
                        )
                )
        );

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(
                        scheduledRequest(
                                startDate,
                                startDate.minusSeconds(1)
                        )
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void deveRejeitarPeriodoQueSeTornaIgualNaPrecisaoDoBanco() {
        LocalDateTime startDate =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        8,
                        0,
                        0,
                        100
                );

        LocalDateTime endDate =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        8,
                        0,
                        0,
                        200
                );

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(
                        scheduledRequest(
                                startDate,
                                endDate
                        )
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void deveRejeitarDescontoPercentualZero() {
        PromotionRequestDTO request =
                new PromotionRequestDTO(
                        "Promo percentual",
                        true,
                        PromotionType.PERCENTAGE,
                        BigDecimal.ZERO,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(request)
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void deveRejeitarDescontoFixoZero() {
        LocalDateTime startDate =
                LocalDateTime.of(
                        2026,
                        8,
                        14,
                        8,
                        0
                );

        PromotionRequestDTO request =
                new PromotionRequestDTO(
                        "Promo agendada",
                        true,
                        PromotionType.SCHEDULED,
                        null,
                        startDate,
                        startDate.plusDays(1),
                        BigDecimal.ZERO,
                        null,
                        null
                );

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(request)
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void deveCriarCupomNormalizadoParaOTenantAutenticado() {
        configureSave();

        when(
                promotionRepository
                        .existsCouponCodeByTenantId(
                                10L,
                                "SAVE10"
                        )
        ).thenReturn(false);

        PromotionResponseDTO response =
                promotionService.create(
                        couponRequest(" save10 ")
                );

        ArgumentCaptor<Promotion> captor =
                ArgumentCaptor.forClass(
                        Promotion.class
                );

        verify(
                promotionRepository
        ).saveAndFlush(captor.capture());

        CouponPromotion saved =
                assertInstanceOf(
                        CouponPromotion.class,
                        captor.getValue()
                );

        assertEquals(
                "SAVE10",
                saved.getCouponCode()
        );
        assertEquals(
                PromotionType.COUPON,
                response.promotionType()
        );
        assertEquals(
                "SAVE10",
                response.couponCode()
        );
    }

    @Test
    void deveRejeitarCupomDuplicadoSomenteNoTenantAutenticado() {
        when(
                promotionRepository
                        .existsCouponCodeByTenantId(
                                10L,
                                "SAVE10"
                        )
        ).thenReturn(true);

        assertThrows(
                CouponCodeAlreadyExistsException.class,
                () -> promotionService.create(
                        couponRequest("save10")
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveRejeitarCupomMaiorQueLimiteAposNormalizacao() {
        String couponCode = "ß".repeat(100);

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.create(
                        couponRequest(couponCode)
                )
        );

        verify(
                promotionRepository,
                never()
        ).existsCouponCodeByTenantId(
                any(),
                any()
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void deveConverterConflitoConcorrenteDeCupomEmErroDeDominio() {
        ConstraintViolationException constraintViolation =
                mock(ConstraintViolationException.class);

        when(
                constraintViolation.getConstraintName()
        ).thenReturn(
                "ux_promotion_tenant_coupon_code"
        );

        when(
                promotionRepository
                        .existsCouponCodeByTenantId(
                                10L,
                                "SAVE10"
                        )
        ).thenReturn(false);

        when(
                promotionRepository.saveAndFlush(
                        any(Promotion.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "duplicate",
                        constraintViolation
                )
        );

        assertThrows(
                CouponCodeAlreadyExistsException.class,
                () -> promotionService.create(
                        couponRequest("save10")
                )
        );

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void devePropagarViolacaoDeOutraConstraint() {
        ConstraintViolationException constraintViolation =
                mock(ConstraintViolationException.class);

        when(
                constraintViolation.getConstraintName()
        ).thenReturn("chk_promotion_rules");

        when(
                promotionRepository
                        .existsCouponCodeByTenantId(
                                10L,
                                "SAVE10"
                        )
        ).thenReturn(false);

        when(
                promotionRepository.saveAndFlush(
                        any(Promotion.class)
                )
        ).thenThrow(
                new DataIntegrityViolationException(
                        "invalid promotion",
                        constraintViolation
                )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> promotionService.create(
                        couponRequest("save10")
                )
        );

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveListarSomentePromocoesDoTenantAutenticado() {
        PercentagePromotion percentagePromotion =
                new PercentagePromotion();

        percentagePromotion.setId(100L);
        percentagePromotion.setName("Percentual");
        percentagePromotion.setTenant(tenant);
        percentagePromotion.setDiscountPercentage(
                new BigDecimal("10.00")
        );

        CouponPromotion couponPromotion =
                new CouponPromotion();

        couponPromotion.setId(101L);
        couponPromotion.setName("Cupom");
        couponPromotion.setTenant(tenant);
        couponPromotion.setCouponCode("SAVE10");
        couponPromotion.setDiscountValue(
                new BigDecimal("10.00")
        );
        couponPromotion.setUsageLimit(5);

        Pageable pageable =
                PageRequest.of(0, 20);

        when(
                promotionRepository
                        .findAllByTenant_Id(
                                10L,
                                pageable
                        )
        ).thenReturn(
                new PageImpl<>(
                        List.of(
                                percentagePromotion,
                                couponPromotion
                        ),
                        pageable,
                        2
                )
        );

        Page<PromotionResponseDTO> result =
                promotionService.findAll(pageable);

        assertEquals(
                2,
                result.getTotalElements()
        );

        assertEquals(
                List.of(
                        PromotionType.PERCENTAGE,
                        PromotionType.COUPON
                ),
                result.getContent()
                        .stream()
                        .map(
                                PromotionResponseDTO
                                        ::promotionType
                        )
                        .toList()
        );

        verify(
                promotionRepository
        ).findAllByTenant_Id(10L, pageable);

        verify(
                promotionRepository,
                never()
        ).findAll();
    }

    @Test
    void deveAtualizarPromocaoPercentualEPreservarProdutos() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setName("Promo antiga");
        promotion.setActive(false);
        promotion.setTenant(tenant);
        promotion.setDiscountPercentage(
                new BigDecimal("5.00")
        );

        Product product = new Product();
        product.setId(200L);

        promotion.addProduct(product);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                promotionRepository
                        .saveAndFlush(promotion)
        ).thenReturn(promotion);

        PromotionResponseDTO response =
                promotionService.update(
                        100L,
                        percentageRequest()
                );

        assertEquals(
                "Promo percentual",
                promotion.getName()
        );
        assertTrue(promotion.isActive());

        assertEquals(
                new BigDecimal("15.00"),
                promotion.getDiscountPercentage()
        );

        assertEquals(
                1,
                promotion.getProducts().size()
        );

        assertTrue(
                promotion.getProducts().contains(product)
        );

        assertEquals(
                new BigDecimal("15.00"),
                response.discountPercentage()
        );

        verify(auditLogService).register(
                "PROMOTION_UPDATE",
                "Promotion",
                "100"
        );
    }

    @Test
    void deveAtualizarPromocaoAgendada() {
        ScheduledPromotion promotion =
                new ScheduledPromotion();

        promotion.setId(100L);
        promotion.setName("Agendada antiga");
        promotion.setActive(false);
        promotion.setTenant(tenant);
        promotion.setStartDate(
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        8,
                        0
                )
        );
        promotion.setEndDate(
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        8,
                        0
                )
        );
        promotion.setDiscountValue(
                new BigDecimal("5.00")
        );

        LocalDateTime newStartDate =
                LocalDateTime.of(
                        2026,
                        9,
                        1,
                        8,
                        0
                );

        LocalDateTime newEndDate =
                newStartDate.plusDays(3);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                promotionRepository
                        .saveAndFlush(promotion)
        ).thenReturn(promotion);

        PromotionResponseDTO response =
                promotionService.update(
                        100L,
                        scheduledRequest(
                                newStartDate,
                                newEndDate
                        )
                );

        assertEquals(
                newStartDate,
                promotion.getStartDate()
        );
        assertEquals(
                newEndDate,
                promotion.getEndDate()
        );
        assertEquals(
                new BigDecimal("25.00"),
                promotion.getDiscountValue()
        );
        assertEquals(
                PromotionType.SCHEDULED,
                response.promotionType()
        );

        verify(auditLogService).register(
                "PROMOTION_UPDATE",
                "Promotion",
                "100"
        );
    }

    @Test
    void deveAtualizarCupomMantendoSeuProprioCodigo() {
        CouponPromotion promotion =
                createExistingCoupon();

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                promotionRepository
                        .existsCouponCodeByTenantIdExcludingPromotionId(
                                10L,
                                100L,
                                "SAVE10"
                        )
        ).thenReturn(false);

        when(
                promotionRepository
                        .saveAndFlush(promotion)
        ).thenReturn(promotion);

        PromotionResponseDTO response =
                promotionService.update(
                        100L,
                        couponRequest(" save10 ")
                );

        assertEquals(
                "SAVE10",
                promotion.getCouponCode()
        );
        assertEquals(
                new BigDecimal("10.00"),
                promotion.getDiscountValue()
        );
        assertEquals(
                5,
                promotion.getUsageLimit()
        );
        assertEquals(
                "SAVE10",
                response.couponCode()
        );

        verify(
                promotionRepository
        ).existsCouponCodeByTenantIdExcludingPromotionId(
                10L,
                100L,
                "SAVE10"
        );

        verify(auditLogService).register(
                "PROMOTION_UPDATE",
                "Promotion",
                "100"
        );
    }

    @Test
    void deveRejeitarCodigoDeOutroCupomAoAtualizar() {
        CouponPromotion promotion =
                createExistingCoupon();

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                promotionRepository
                        .existsCouponCodeByTenantIdExcludingPromotionId(
                                10L,
                                100L,
                                "SAVE10"
                        )
        ).thenReturn(true);

        assertThrows(
                CouponCodeAlreadyExistsException.class,
                () -> promotionService.update(
                        100L,
                        couponRequest("save10")
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveRejeitarAlteracaoDoTipoDaPromocao() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        assertThrows(
                InvalidPromotionException.class,
                () -> promotionService.update(
                        100L,
                        couponRequest("SAVE10")
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveOcultarPromocaoDeOutroTenantNaAtualizacao() {
        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                PromotionNotFoundException.class,
                () -> promotionService.update(
                        100L,
                        percentageRequest()
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveAssociarProdutoDoMesmoTenant() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        Product product = new Product();
        product.setId(200L);
        product.setTenant(tenant);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                productRepository
                        .findByIdAndTenant_Id(
                                200L,
                                10L
                        )
        ).thenReturn(Optional.of(product));

        boolean associated =
                promotionService.associateProduct(
                        100L,
                        200L
                );

        assertTrue(associated);
        assertTrue(
                promotion.getProducts().contains(product)
        );
        assertTrue(
                product.getPromotions().contains(promotion)
        );

        verify(
                promotionRepository
        ).saveAndFlush(promotion);

        verify(auditLogService).register(
                "PROMOTION_PRODUCT_ASSOCIATE",
                "PromotionProduct",
                "100:200"
        );
    }

    @Test
    void deveIgnorarProdutoJaAssociado() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        Product product = new Product();
        product.setId(200L);
        product.setTenant(tenant);

        promotion.addProduct(product);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                productRepository
                        .findByIdAndTenant_Id(
                                200L,
                                10L
                        )
        ).thenReturn(Optional.of(product));

        boolean associated =
                promotionService.associateProduct(
                        100L,
                        200L
                );

        assertFalse(associated);

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveDesassociarProdutoDoMesmoTenant() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        Product product = new Product();
        product.setId(200L);
        product.setTenant(tenant);

        promotion.addProduct(product);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                productRepository
                        .findByIdAndTenant_Id(
                                200L,
                                10L
                        )
        ).thenReturn(Optional.of(product));

        boolean disassociated =
                promotionService.disassociateProduct(
                        100L,
                        200L
                );

        assertTrue(disassociated);

        assertFalse(
                promotion.getProducts().contains(product)
        );

        assertFalse(
                product.getPromotions().contains(promotion)
        );

        verify(
                promotionRepository
        ).saveAndFlush(promotion);

        verify(auditLogService).register(
                "PROMOTION_PRODUCT_DISASSOCIATE",
                "PromotionProduct",
                "100:200"
        );
    }

    @Test
    void deveRejeitarProdutoDeOutroTenantNaAssociacao() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        when(
                productRepository
                        .findByIdAndTenant_Id(
                                200L,
                                10L
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> promotionService.associateProduct(
                        100L,
                        200L
                )
        );

        verify(
                promotionRepository,
                never()
        ).saveAndFlush(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveRemoverSomentePromocaoDoTenantAutenticadoEAuditar() {
        PercentagePromotion promotion =
                new PercentagePromotion();

        promotion.setId(100L);
        promotion.setTenant(tenant);

        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.of(promotion));

        promotionService.delete(100L);

        verify(
                promotionRepository
        ).delete(promotion);

        verify(auditLogService).register(
                "PROMOTION_DELETE",
                "Promotion",
                "100"
        );
    }

    @Test
    void deveOcultarPromocaoDeOutroTenantNaRemocao() {
        when(
                promotionRepository
                        .findByIdAndTenant_Id(
                                100L,
                                10L
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                PromotionNotFoundException.class,
                () -> promotionService.delete(100L)
        );

        verify(
                promotionRepository,
                never()
        ).delete(any());

        verify(
                auditLogService,
                never()
        ).register(any(), any(), any());
    }

    @Test
    void deveNegarUsuarioAutenticadoSemTenant() {
        admin.setTenant(null);

        assertThrows(
                AccessDeniedException.class,
                () -> promotionService.findAll(
                        PageRequest.of(0, 20)
                )
        );

        verify(
                promotionRepository,
                never()
        ).findAllByTenant_Id(any(), any());
    }

    private void configureSave() {
        when(
                promotionRepository.saveAndFlush(
                        any(Promotion.class)
                )
        ).thenAnswer(invocation -> {
            Promotion promotion =
                    invocation.getArgument(0);

            promotion.setId(100L);

            return promotion;
        });
    }

    private CouponPromotion createExistingCoupon() {
        CouponPromotion promotion =
                new CouponPromotion();

        promotion.setId(100L);
        promotion.setName("Cupom antigo");
        promotion.setActive(true);
        promotion.setTenant(tenant);
        promotion.setCouponCode("OLD10");
        promotion.setDiscountValue(
                new BigDecimal("5.00")
        );
        promotion.setUsageLimit(2);

        return promotion;
    }

    private PromotionRequestDTO percentageRequest() {
        return new PromotionRequestDTO(
                "  Promo percentual  ",
                true,
                PromotionType.PERCENTAGE,
                new BigDecimal("15.00"),
                null,
                null,
                null,
                null,
                null
        );
    }

    private PromotionRequestDTO scheduledRequest(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        return new PromotionRequestDTO(
                "Promo agendada",
                true,
                PromotionType.SCHEDULED,
                null,
                startDate,
                endDate,
                new BigDecimal("25.00"),
                null,
                null
        );
    }

    private PromotionRequestDTO couponRequest(
            String couponCode
    ) {
        return new PromotionRequestDTO(
                "Cupom",
                true,
                PromotionType.COUPON,
                null,
                null,
                null,
                new BigDecimal("10.00"),
                couponCode,
                5
        );
    }
}