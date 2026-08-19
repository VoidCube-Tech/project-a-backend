package com.voidcube.tech.projectA.analyticsevent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    
    
}
