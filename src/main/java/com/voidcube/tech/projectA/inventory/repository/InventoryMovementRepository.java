package com.voidcube.tech.projectA.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.inventory.model.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    
    
}
