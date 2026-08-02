package com.voidcube.tech.projectA.shared.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.voidcube.tech.projectA.user.model.User;

@Component
public class SecurityUtils {
    
    public User getAuthenticadedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário autenticado não encontrado");
        }
        return user;
    }

    public Long requireAuthenticatedTenantId() {
        User user = getAuthenticadedUser();

        if(user.getTenant() == null) {
            throw new AccessDeniedException("Esta operação exige um usuário vinculado a um tenant");
        }
        
        return user.getTenant().getId();
    }
}
