package com.voidcube.tech.projectA.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
public record AppFrontendProperties(String url) {
    
}
