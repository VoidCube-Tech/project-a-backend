package com.voidcube.tech.projectA.landingPage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LandingPageRequestDTO(
    @NotBlank(message = "Nome obrigatório")
    @Size(max = 255)
    String name,

    @NotBlank(message = "Dominio obrigatório")
    @Size(max = 255)
    @Pattern(regexp = "^[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*$",
            message = "O domínio deve usar apenas letras, números e hífens."
    )
    String domainUrl,

    @Size(max = 30)
    @Pattern(regexp = "^$|^\\+?[0-9()\\s-]+$",
            message = "O número possui caracteres inválidos"
    )
    String whatsappNumber


) {} 
