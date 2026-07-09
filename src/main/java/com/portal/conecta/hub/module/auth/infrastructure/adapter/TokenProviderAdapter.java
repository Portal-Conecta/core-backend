package com.portal.conecta.hub.module.auth.infrastructure.adapter;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Implementação JWT de {@link TokenProviderPort} usando a biblioteca JJWT.
 *
 * <p>Os tempos de expiração e a chave secreta são lidos de {@link JwtProperties},
 * configurada via {@code app.jwt.*} no {@code application.yml}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProviderAdapter implements TokenProviderPort {

    private final JwtProperties jwtProperties;

    /**
     * {@inheritDoc}
     *
     * <p>A lista de turmas é mapeada para {@link ContextClass} antes de ser embutida
     * no token como claim {@code classes}.</p>
     */
    @Override
    public String generateAccessToken(AuthUser authUser, List<ClassMembershipEntity> classMembershipEntities) {
        List<ContextClass> classes = classMembershipEntities
                .stream()
                .map(membership -> new ContextClass(
                        membership.getId().getClassId(),
                        membership.getClassRole()
                ))
                .toList();

        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.getId().toString())
                .claim("userType", authUser.getType().name())
                .claim("classes", classes)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        log.info("Access token gerado para usuário.");
        return token;
    }

    @Override
    public String generateRefreshToken(AuthUser authUser) {
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(authUser.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        log.info("Refresh token gerado para usuário.");
        return token;
    }

    /**
     * {@inheritDoc}
     *
     * <p>A validação é feita localmente via assinatura HMAC-SHA, sem consulta
     * ao banco de dados. Tokens estruturalmente válidos mas revogados só são
     * detectados pelo {@link RefreshTokenRepository}.</p>
     */
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
                throw new AuthException("Tipo de token inválido");
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new AuthException("Refresh token inválido ou expirado");
            }

            UUID userId = UUID.fromString(subject);
            log.info("Refresh token validado com sucesso para usuário");
            return userId;

        } catch (AuthException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException("Refresh token inválido ou expirado");
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
        // Secret deve ser Base64 de no mínimo 32 bytes (256 bits) — requisito do HMAC-SHA256
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}