package com.voidcube.tech.projectA.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.voidcube.tech.projectA.plan.model.Plan;
import com.voidcube.tech.projectA.plan.repository.PlanRepository;
import com.voidcube.tech.projectA.shared.config.AppFrontendProperties;
import com.voidcube.tech.projectA.shared.exception.EmailAlreadyExistsException;
import com.voidcube.tech.projectA.shared.exception.InvalidTokenException;
import com.voidcube.tech.projectA.shared.exception.TokenExpiredException;
import com.voidcube.tech.projectA.shared.service.EmailService;
import com.voidcube.tech.projectA.tenant.model.Tenant;
import com.voidcube.tech.projectA.tenant.repository.TenantRepository;
import com.voidcube.tech.projectA.user.dto.request.RegisterRequestDTO;
import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import com.voidcube.tech.projectA.user.model.VerificationToken;
import com.voidcube.tech.projectA.user.repository.UserRepository;
import com.voidcube.tech.projectA.user.repository.VerificationTokenRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepositoy;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppFrontendProperties frontendProperties;
    private final PlanRepository planRepository;

    @Transactional
    public void register(RegisterRequestDTO request) {
        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Este e-mail já está cadastrado");
        }

        Plan basicPlan = planRepository.findByName("Basic")
            .orElseThrow(()-> new IllegalStateException("O plano padrão Basic não está cadastrado"));

        Tenant tenant = new Tenant();
        tenant.setCompanyName(request.companyName());
        tenant.setPlan(basicPlan);
        tenantRepositoy.save(tenant);

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_ADMIN);
        user.setTenant(tenant);
        userRepository.save(user);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);

        String verificationLink = frontendProperties.url() + "/verificar?token=" + verificationToken.getToken();
        emailService.sendVerificationEmail(user.getEmail(), verificationLink);
    }
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(()-> new InvalidTokenException("Token inválido ou expirado"));

            User user = verificationToken.getUser();
            
            if(user.getEmailVerifiedAt() != null) {
                return;
            }

            if(verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new TokenExpiredException("Token Expirado. Solicite um novo e-mail de verificação");
            }
            
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
    }


}
