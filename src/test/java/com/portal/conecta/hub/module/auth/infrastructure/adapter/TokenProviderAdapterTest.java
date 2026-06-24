package com.portal.conecta.hub.module.auth.infrastructure.adapter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.auth.infrastructure.adapter").setLevel(Level.INFO);

        userId = UUID.randomUUID();
        authUser = new AuthUser() {
            public UUID getId() { return userId; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return true; }
        };
    }

    @Test
    void refreshTokenContainsTypeClaimAndSubject(CapturedOutput output) {
        String token = adapter.generateRefreshToken(authUser);

        Claims claims = parse(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
        assertNotNull(claims.getId());

        assertThat(output).contains("gerado para");
        assertNoTokenLeaked(output, token);
    }

    @Test
    void refreshTokenDoesNotContainPermissions() {
        String token = adapter.generateRefreshToken(authUser);

        Claims claims = parse(token);
        assertNull(claims.get("userType"));
        assertNull(claims.get("classes"));
    }

    @Test
    void validateRefreshTokenReturnsUserId(CapturedOutput output) {
        String token = adapter.generateRefreshToken(authUser);

        UUID result = adapter.validateRefreshToken(token);

        assertEquals(userId, result);
        assertThat(output).contains("validado com sucesso para");
        assertNoTokenLeaked(output, token);
    }

    @Test
    void validateRefreshTokenThrowsWhenTokenIsExpired(CapturedOutput output) {
        String expired = Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 5_000))
                .signWith(signingKey())
                .compact();

        assertThrows(AuthException.class, () -> adapter.validateRefreshToken(expired));

        assertNoTokenLeaked(output, expired);
    }

    @Test
    void validateRefreshTokenThrowsWhenSignatureIsInvalid(CapturedOutput output) {
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
    void validateRefreshTokenThrowsWhenTypeIsNotRefresh(CapturedOutput output) {
        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("userType", "STUDENT")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(signingKey())
                .compact();

        assertThrows(AuthException.class,
                () -> adapter.validateRefreshToken(accessToken));

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

    private void assertNoTokenLeaked(CapturedOutput output, String token) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain(token.toLowerCase());
        assertThat(out).doesNotContain("authorization");
    }
}