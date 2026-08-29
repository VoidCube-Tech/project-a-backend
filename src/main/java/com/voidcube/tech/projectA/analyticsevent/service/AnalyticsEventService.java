package com.voidcube.tech.projectA.analyticsevent.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voidcube.tech.projectA.analyticsevent.dto.request.AnalyticsEventRequestDTO;
import com.voidcube.tech.projectA.analyticsevent.model.AnalyticsEvent;
import com.voidcube.tech.projectA.analyticsevent.model.EventType;
import com.voidcube.tech.projectA.analyticsevent.repository.AnalyticsEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsEventService {
    
    private final AnalyticsEventRepository analyticsEventRepository;


    @Async("analyticsTaskExecutor")
    @Transactional
    public void saveAsync(AnalyticsEventRequestDTO request) {
       save(request.landingPageId(), request.productId(), request.eventType());
    }

    @Async("analyticsTaskExecutor")
    @Transactional
    public void saveWhatsappClickAsync(Long landingPageId, Long productId) {
        save(landingPageId, productId, EventType.WHATSAPP_CLICK);
    }

    private void save(Long landingPageId, Long productId, EventType eventType) {
        AnalyticsEvent event = new AnalyticsEvent();

        event.setLandingPageId(landingPageId);
        event.setProductId(productId);
        event.setEventType(eventType);

        analyticsEventRepository.save(event);
    }
}
