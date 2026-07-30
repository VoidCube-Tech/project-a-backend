package com.voidcube.tech.projectA.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(String from, String verificationSubject) {
}
