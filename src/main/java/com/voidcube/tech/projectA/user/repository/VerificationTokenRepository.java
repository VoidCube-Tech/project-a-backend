package com.voidcube.tech.projectA.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.user.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository <VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
}
