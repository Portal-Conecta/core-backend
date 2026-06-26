package com.portal.conecta.hub.module.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades JWT lidas do prefixo {@code app.jwt.*} no {@code application.yml}.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpiration,
        Long refreshTokenExpiration
) { }