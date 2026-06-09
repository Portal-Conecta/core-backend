package com.portal.conecta.hub.module.auth.infrastructure.adapter;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.shared.context.ContextClass;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProviderPort {

    private final JwtProperties jwtProperties;

    @Override
    public String generateAccessToken(AuthUser authUser, List<ClassMembershipEntity> classMembershipEntities) {

        List<ContextClass> classes = classMembershipEntities
                .stream()
                .map(membership -> new ContextClass(
                        membership.getId().getClassId(),
                        membership.getClassRole()
                ))
                .toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.getId().toString())
                .claim("userType", authUser.getType().name())
                .claim("classes", classes)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(AuthUser authUser) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public UUID validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();

            String tokenType = claims.get("type", String.class);

            if (!"refresh".equals(tokenType)) {
                throw new AuthException("Invalid token type");
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new AuthException("Invalid or expired refresh token");
            }

            return UUID.fromString(subject);
        } catch (AuthException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException("Invalid or expired refresh token");
        }
    }

    @Override
    public Long getAccessTokenExpirationMs() {
        return jwtProperties.accessTokenExpiration();
    }

    @Override
    public Long getRefreshTokenExpirationMs() {
        return jwtProperties.refreshTokenExpiration();
    }

    private SecretKey getSigningKey() {
       byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
       return Keys.hmacShaKeyFor(keyBytes);
    }

}