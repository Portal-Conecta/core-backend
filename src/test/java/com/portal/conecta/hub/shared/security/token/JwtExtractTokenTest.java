package com.portal.conecta.hub.shared.security.token;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.security.user.CustomUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtExtractTokenTest {
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final String OTHER_SECRET = "ZmVkY2JhOTg3NjU0MzIxMGZlZGNiYTk4NzY1NDMyMTA=";
    private final JwtExtractToken extractor = new JwtExtractToken();

    @BeforeEach void setUp() { ReflectionTestUtils.setField(extractor, "secret", SECRET); }

    @Test void validatesValidTokenAndRejectsExpiredMalformedAndWrongSignature() {
        assertTrue(extractor.isValidToken(token(SECRET, List.of(), Instant.now().plusSeconds(60))));
        assertFalse(extractor.isValidToken(token(SECRET, List.of(), Instant.now().minusSeconds(1))));
        assertFalse(extractor.isValidToken("not-a-jwt"));
        assertFalse(extractor.isValidToken(token(OTHER_SECRET, List.of(), Instant.now().plusSeconds(60))));
    }

    @Test void extractsIdentityPermissionVersionAndClasses() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        String token = token(SECRET, List.of(Map.of("classId", classId.toString(), "role", "TEACHER")), Instant.now().plusSeconds(60), userId);
        CustomUserDetails details = extractor.extractUserDetails(token);
        RequestContext context = details.toRequestContext();
        assertEquals(userId, context.userId());
        assertEquals(TypeUser.ADMIN, context.userType());
        assertEquals(classId, context.classes().getFirst().classId());
        assertEquals(ClassRole.TEACHER, context.classes().getFirst().role());
        assertEquals("7", ReflectionTestUtils.getField(details, "permissionVersion"));
    }

    @Test void missingOrNonListClassesBecomeEmpty() {
        assertTrue(extractor.extractUserDetails(tokenWithoutClasses()).toRequestContext().classes().isEmpty());
        assertTrue(extractor.extractUserDetails(token(SECRET, "invalid", Instant.now().plusSeconds(60))).toRequestContext().classes().isEmpty());
    }

    @Test void invalidClassEntriesFailExplicitly() {
        assertThrows(IllegalArgumentException.class, () -> extractor.extractUserDetails(token(SECRET, List.of("invalid"), Instant.now().plusSeconds(60))));
        assertThrows(IllegalArgumentException.class, () -> extractor.extractUserDetails(token(SECRET, List.of(Map.of("role", "STUDENT")), Instant.now().plusSeconds(60))));
        assertThrows(IllegalArgumentException.class, () -> extractor.extractUserDetails(token(SECRET, List.of(Map.of("classId", UUID.randomUUID().toString())), Instant.now().plusSeconds(60))));
        assertThrows(IllegalArgumentException.class, () -> extractor.extractUserDetails(token(SECRET, List.of(Map.of("classId", UUID.randomUUID().toString(), "role", "INVALID")), Instant.now().plusSeconds(60))));
    }

    @Test void acceptsAlreadyTypedClassClaims() {
        UUID classId = UUID.randomUUID();
        com.portal.conecta.hub.shared.context.ContextClass contextClass =
                new com.portal.conecta.hub.shared.context.ContextClass(classId, ClassRole.STUDENT);
        @SuppressWarnings("unchecked")
        List<com.portal.conecta.hub.shared.context.ContextClass> classes =
                (List<com.portal.conecta.hub.shared.context.ContextClass>) ReflectionTestUtils.invokeMethod(
                        extractor, "extractClasses", List.of(contextClass));
        assertNotNull(classes);
        assertSame(contextClass, classes.getFirst());
    }

    private String token(String secret, Object classes, Instant expiration) { return token(secret, classes, expiration, UUID.randomUUID()); }

    private String token(String secret, Object classes, Instant expiration, UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder().subject(userId.toString()).claim("userType", "ADMIN")
                .claim("permissionVersion", "7").claim("classes", classes)
                .expiration(Date.from(expiration)).signWith(key).compact();
    }

    private String tokenWithoutClasses() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder().subject(UUID.randomUUID().toString()).claim("userType", "ADMIN")
                .claim("permissionVersion", "7").expiration(Date.from(Instant.now().plusSeconds(60))).signWith(key).compact();
    }
}
