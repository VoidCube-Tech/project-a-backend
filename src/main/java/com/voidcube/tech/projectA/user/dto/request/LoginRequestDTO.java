package com.voidcube.tech.projectA.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO (

    @NotBlank(message = "{validation.auth.email.required}")
    @Email(message = "{validation.auth.email.invalid}")
    String email,

    @NotBlank(message = "{validation.auth.password.required}")
    String password
)
{}

