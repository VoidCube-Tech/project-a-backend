package com.voidcube.tech.projectA.user.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.user.dto.LoginRequestDTO;
import com.voidcube.tech.projectA.user.dto.RegisterRequesteDTO;
import com.voidcube.tech.projectA.user.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        httpRequest.getSession(true);

        return "Login realizado com sucesso";
    } 

    @PostMapping("/register")
    public String register (@Valid @RequestBody RegisterRequesteDTO dto) {
        authService.register(dto);
        return "Cadastro realizado com sucesso. Verifique seu email na caixa de mensagens";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return "E-mail verificado com sucesso. Você já pode fazer login";
    }
}

