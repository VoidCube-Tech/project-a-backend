package com.voidcube.tech.projectA.shared.config;

import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class InternationalizationConfig {

    private static final Locale PT_BR =
            Locale.forLanguageTag("pt-BR");

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver =
                new AcceptHeaderLocaleResolver();

        resolver.setDefaultLocale(PT_BR);
        resolver.setSupportedLocales(
                List.of(PT_BR)
        );

        return resolver;
    }
}