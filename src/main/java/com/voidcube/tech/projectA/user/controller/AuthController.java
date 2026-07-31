package com.voidcube.tech.projectA.user.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.user.dto.LoginRequestDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest ) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
);
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        httpRequest.getSession(true);

        return "Login realizado com sucesso";
    } 
}

