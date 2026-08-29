package com.voidcube.tech.projectA.shared.routing;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.voidcube.tech.projectA.analyticsevent.controller.AnalyticsEventController;
import com.voidcube.tech.projectA.audit.controller.AuditLogController;
import com.voidcube.tech.projectA.dashboard.controller.DashboardMetricsController;
import com.voidcube.tech.projectA.export.controller.ProductExportController;
import com.voidcube.tech.projectA.landingpage.controller.LandingPageController;
import com.voidcube.tech.projectA.landingpage.controller.PublicLandingPageController;
import com.voidcube.tech.projectA.product.controller.ProductController;
import com.voidcube.tech.projectA.product.controller.ProductImageController;
import com.voidcube.tech.projectA.product.controller.ProductImageFileController;
import com.voidcube.tech.projectA.product.controller.PublicProductImageFileController;
import com.voidcube.tech.projectA.promotion.controller.PromotionController;
import com.voidcube.tech.projectA.tenant.controller.AdminTenantController;
import com.voidcube.tech.projectA.user.controller.AuthController;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest
class ControllerRouteMappingTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @TestFactory
    Stream<DynamicTest> deveRegistrarRotasCorretamente() {
        return expectedRoutes()
                .stream()
                .map(route ->
                        dynamicTest(
                                route.method()
                                        + " "
                                        + route.path(),
                                () -> assertRouteExists(route)
                        )
                );
    }

    private void assertRouteExists(ExpectedRoute route) {
        boolean routeExists = handlerMapping
                .getHandlerMethods()
                .entrySet()
                .stream()
                .anyMatch(entry ->
                        entry.getKey()
                                .getMethodsCondition()
                                .getMethods()
                                .contains(route.method())
                        &&
                        entry.getKey()
                                .getPatternValues()
                                .contains(route.path())
                        &&
                        entry.getValue()
                                .getBeanType()
                                .equals(route.controller())
                );

        assertTrue(
                routeExists,
                () -> "Rota não registrada: "
                        + route.method()
                        + " "
                        + route.path()
                        + " no controller "
                        + route.controller().getSimpleName()
        );
    }

    private List<ExpectedRoute> expectedRoutes() {
        return List.of(
                route(
                        RequestMethod.POST,
                        "/api/v1/public/analytics/events",
                        AnalyticsEventController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/admin/audit",
                        AuditLogController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/dashboard/metrics",
                        DashboardMetricsController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/export/products",
                        ProductExportController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/landing-pages",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/landing-pages",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.PUT,
                        "/api/v1/landing-pages/{id}",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/landing-pages/{id}",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/landing-pages/{pageId}/products/{productId}",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/landing-pages/{pageId}/products/{productId}",
                        LandingPageController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/public/landing-pages/{domainUrl}",
                        PublicLandingPageController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/public/landing-pages/{domainUrl}/whatsapp",
                        PublicLandingPageController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/products",
                        ProductController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/products",
                        ProductController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/products/{id}",
                        ProductController.class
                ),
                route(
                        RequestMethod.PUT,
                        "/api/v1/products/{id}",
                        ProductController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/products/{id}",
                        ProductController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/products/{productId}/images",
                        ProductImageController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/products/{productId}/images",
                        ProductImageController.class
                ),
                route(
                        RequestMethod.PATCH,
                        "/api/v1/products/{productId}/images/{imageId}/main",
                        ProductImageController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/products/{productId}/images/{imageId}",
                        ProductImageController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/products/{productId}/images/{imageId}/file",
                        ProductImageFileController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/public/product-images/{imageId}/file",
                        PublicProductImageFileController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/promotions",
                        PromotionController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/promotions",
                        PromotionController.class
                ),
                route(
                        RequestMethod.PUT,
                        "/api/v1/promotions/{id}",
                        PromotionController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/promotions/{id}",
                        PromotionController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/promotions/{promotionId}/products/{productId}",
                        PromotionController.class
                ),
                route(
                        RequestMethod.DELETE,
                        "/api/v1/promotions/{promotionId}/products/{productId}",
                        PromotionController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/admin/tenants",
                        AdminTenantController.class
                ),
                route(
                        RequestMethod.PUT,
                        "/api/v1/admin/tenants/{tenantId}/plan",
                        AdminTenantController.class
                ),

                route(
                        RequestMethod.GET,
                        "/api/v1/auth/csrf",
                        AuthController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/auth/login",
                        AuthController.class
                ),
                route(
                        RequestMethod.POST,
                        "/api/v1/auth/register",
                        AuthController.class
                ),
                route(
                        RequestMethod.GET,
                        "/api/v1/auth/verify-email",
                        AuthController.class
                )
        );
    }

    private ExpectedRoute route(
            RequestMethod method,
            String path,
            Class<?> controller
    ) {
        return new ExpectedRoute(
                method,
                path,
                controller
        );
    }

    private record ExpectedRoute(
            RequestMethod method,
            String path,
            Class<?> controller
    ) {}
}
