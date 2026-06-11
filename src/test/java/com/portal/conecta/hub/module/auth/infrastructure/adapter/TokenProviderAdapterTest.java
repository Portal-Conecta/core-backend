package com.portal.conecta.hub.module.auth.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class TokenProviderAdapterTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("test-secret-key-32-bytes-minimum!!".getBytes());

    @Mock
    private JwtProperties jwtProperties;

    private TokenProviderAdapter adapter;
    private UUID userId;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        when(jwtProperties.secret()).thenReturn(SECRET);
        when(jwtProperties.accessTokenExpiration()).thenReturn(900_000L);
        when(jwtProperties.refreshTokenExpiration()).thenReturn(604_800_000L);

        adapter = new TokenProviderAdapter(jwtProperties);

        userId = UUID.randomUUID();
        authUser = new AuthUser() {
            public UUID getId() { return userId; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return true; }
        };
    }

    @Test
    void refreshTokenContainsTypeClaimAndSubject() {
        String token = adapter.generateRefreshToken(authUser);

        Claims claims = parse(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertNotNull(claims.getId());
    }

    @Test
    void refreshTokenDoesNotContainPermissions() {
        String token = adapter.generateRefreshToken(authUser);

        Claims claims = parse(token);
        assertNull(claims.get("userType"));
        assertNull(claims.get("classes"));
    }

    @Test
    void validateRefreshTokenReturnsUserId() {
        String token = adapter.generateRefreshToken(authUser);

        UUID result = adapter.validateRefreshToken(token);

        assertEquals(userId, result);
    }

    @Test
    void validateRefreshTokenThrowsWhenTokenIsExpired() {
        String expired = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(signingKey())
                .compact();

        assertThrows(AuthException.class, () -> adapter.validateRefreshToken(expired));
    }

    @Test
    void validateRefreshTokenThrowsWhenSignatureIsInvalid() {
        String wrongSecret = Base64.getEncoder().encodeToString("wrong-secret-key-32-bytes-minimum!!".getBytes());
        SecretKey wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(wrongSecret));

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        assertThrows(AuthException.class, () -> adapter.validateRefreshToken(token));
    }

    @Test
    void validateRefreshTokenThrowsWhenTypeIsNotRefresh() {
        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("userType", "STUDENT")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey())
                .compact();

        AuthException ex = assertThrows(AuthException.class,
                () -> adapter.validateRefreshToken(accessToken));

        assertEquals("Invalid token type", ex.getMessage());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }
}