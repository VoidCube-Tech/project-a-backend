package com.voidcube.tech.projectA.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
}
