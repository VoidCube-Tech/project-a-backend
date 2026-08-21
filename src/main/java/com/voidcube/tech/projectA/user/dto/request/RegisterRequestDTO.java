package com.voidcube.tech.projectA.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank @Size(max = 255, message = "no máximo 255 caracteres") String companyName,
    @NotBlank @Email @Size(max = 255, message = "no máximo 255 caracteres") String email,
    @NotBlank @Size(min = 8, max = 72, message = "Senha deve possuir no minímo 8 e no maxímo 72 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\-_]).*$" , 
    message = "A senha deve conter uma letra maiúscula, uma minúscula, um número e caractere especial")
    String password
) {}
    

