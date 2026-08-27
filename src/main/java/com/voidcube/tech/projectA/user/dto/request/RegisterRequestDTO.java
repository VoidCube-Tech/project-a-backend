package com.voidcube.tech.projectA.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank(message = "{validation.auth.company-name.required}") 
    @Size(max = 255, message = "{validation.auth.company-name.size}") 
    String companyName,

    @NotBlank(message = "{validation.auth.email.required}") 
    @Email(message = "{validation.auth.email.invalid}") 
    @Size(max = 255, message = "{validation.auth.email.size}") 
    String email,

    @NotBlank(message = "{validation.auth.password.required}") @Size(min = 8, max = 72, message = "{validation.auth.password.size}")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\-_]).*$" , 
    message = "{validation.auth.password.pattern}")
    String password
) {}
    

