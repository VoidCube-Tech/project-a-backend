package com.voidcube.tech.projectA.plan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.plan.model.Plan;

public interface PlanRepository  extends JpaRepository<Plan, Long>{

    Optional<Plan> findByName(String name);
    
}
