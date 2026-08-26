package com.voidcube.tech.projectA.analyticsevent.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsEventService {
    
    private final AnalyticsEventRepository analyticsEventRepository;


    @Async("analyticsTaskExecutor")
    @Transactional
    public void saveAsync(AnalyticsEventRequestDTO request) {
        AnalyticsEvent event = new AnalyticsEvent();

        event.setLandingPageId(request.landingPageId());
        event.setProductId(request.productId());
        event.setEventType(request.eventType());

        analyticsEventRepository.save(event);
    }
}
