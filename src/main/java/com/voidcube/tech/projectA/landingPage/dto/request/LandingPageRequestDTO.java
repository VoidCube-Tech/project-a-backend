package com.voidcube.tech.projectA.landingpage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LandingPageRequestDTO(
    @NotBlank(message = "{validation.landing-page.name.required}")
    @Size(max = 255, message = "{validation.landing-page.name.size}")
    String name,

    @NotBlank(message = "{validation.landing-page.domain.required}")
    @Size(max = 255, message = "{validation.landing-page.domain.size}")
    @Pattern(regexp = "^[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*$",
        message = "validation.landing-page.domain.pattern}")
    String domainUrl,

    @Size(max = 30, message = "{validation.landing-page.whatsapp.size}")
    @Pattern(regexp = "^$|^\\+?[0-9()\\s-]+$",
        message = "{validation.landing-page.whatsapp.pattern}")
    String whatsappNumber


) {} 
