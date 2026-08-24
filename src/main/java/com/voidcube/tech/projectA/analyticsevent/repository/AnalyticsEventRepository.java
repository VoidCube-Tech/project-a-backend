package com.voidcube.tech.projectA.analyticsevent.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.projection.AnalyticsEventCountProjection;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    
    @Query("""
            SELECT event.productId AS productId,
            event.eventType AS eventType,
            COUNT(event.id) AS eventCount
            FROM AnalyticsEvent event WHERE event.productId IN :productIds
            GROUP BY
                event.productId,
                event.eventType
                ORDER BY COUNT(event.id) DESC
            """)
            List<AnalyticsEventCountProjection> countEventsByProductIds(@Param("productIds") Collection<Long> productIds);

}
