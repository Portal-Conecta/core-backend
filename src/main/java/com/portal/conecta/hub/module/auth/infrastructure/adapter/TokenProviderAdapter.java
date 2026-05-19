package com.portal.conecta.hub.module.auth.infrastructure.adapter;

import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProviderPort {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(AuthUser authUser) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.userId().toString())
                .claim("userType", authUser.userType().name())
                .claim("classes", authUser.classes())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(AuthUser authUser) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.userId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
       byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
       return Keys.hmacShaKeyFor(keyBytes);
    }

}