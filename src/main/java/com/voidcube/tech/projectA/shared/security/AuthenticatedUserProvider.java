package com.voidcube.tech.projectA.shared.security;

import com.voidcube.tech.projectA.user.model.Role;
import com.voidcube.tech.projectA.user.model.User;
import com.voidcube.tech.projectA.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                    instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(
                    "Não existe usuário autenticado"
            );
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário autenticado não encontrado: "
                                + email
                ));
    }

    public boolean isSuperAdmin() {
        return getAuthenticatedUser().getRole()
                == Role.ROLE_SUPER_ADMIN;
    }

    public Long getRequiredTenantId() {
        User authenticatedUser = getAuthenticatedUser();

        if (authenticatedUser.getTenant() == null) {
            throw new AccessDeniedException(
                    "O usuário não possui tenant"
            );
        }

        return authenticatedUser.getTenant().getId();
    }
}
