package com.voidcube.tech.projectA.analyticsevent.projection;

import com.voidcube.tech.projectA.analyticsevent.model.EventType;

public interface AnalyticsEventCountProjection {

    Long getProductId();

    EventType getEventType();

    Long getEventCount();
    
}
