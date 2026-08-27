package com.voidcube.tech.projectA.analyticsevent.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
            DELETE FROM analytics_event
            WHERE id IN (
                SELECT id
                FROM analytics_event
                WHERE created_at < :cutoff
                ORDER BY id
                LIMIT :batchSize
                )
            """, nativeQuery = true)
    int deleteBatchCreatedBefore(
        @Param("cutoff") LocalDateTime cutoff,
        @Param("batchSize") int batchsize
    );

}
