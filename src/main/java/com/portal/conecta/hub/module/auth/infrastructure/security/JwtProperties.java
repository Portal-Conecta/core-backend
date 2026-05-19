package com.portal.conecta.hub.module.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Long accessTokenExpiration,
        Long refreshTokenExpiration
) { }