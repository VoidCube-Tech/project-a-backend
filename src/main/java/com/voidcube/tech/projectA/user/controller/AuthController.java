package com.voidcube.tech.projectA.user.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voidcube.tech.projectA.shared.service.MessageService;
import com.voidcube.tech.projectA.user.dto.request.LoginRequestDTO;
import com.voidcube.tech.projectA.user.dto.request.RegisterRequestDTO;
import com.voidcube.tech.projectA.user.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final MessageService messageService;
    private final SecurityContextRepository securityContextRepository;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest, HttpServletResponse httpResponse  ) {
       UsernamePasswordAuthenticationToken authenticationRequest = UsernamePasswordAuthenticationToken
        .unauthenticated(request.email(), request.password());
        
        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, httpResponse);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

       return messageService.get("auth.login.success");
    } 

    @PostMapping("/register")
    public String register (@Valid @RequestBody RegisterRequestDTO dto) {
        authService.register(dto);
        return messageService.get("auth.register.success");
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return messageService.get("auth.email-verification.success");
    }
}

