package com.portal.conecta.hub.shared.security.token;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.shared.context.ContextClass;
import com.portal.conecta.hub.shared.security.user.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtExtractToken {

    @Value("${app.jwt.secret}")
    private String secret;

    public CustomUserDetails extractUserDetails(String token){
        Claims claims = extractClaims(token);

        String userId = claims.getSubject();
        String userType = claims.get("userType", String.class);
        String permissionVersion = claims.get("permissionVersion", String.class);
        List<ContextClass> classes = extractClasses(claims.get("classes"));

        return new CustomUserDetails(userId, userType, classes, permissionVersion);
    }

    public boolean isValidToken(String token){
        try{
            Claims claims = extractClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private List<ContextClass> extractClasses(Object classesClaim) {
        if (!(classesClaim instanceof List<?> values)) {
            return List.of();
        }

        return values.stream()
                .map(this::extractClass)
                .toList();
    }

    private ContextClass extractClass(Object classClaim) {
        if (classClaim instanceof ContextClass contextClass) {
            return contextClass;
        }

        if (!(classClaim instanceof Map<?, ?> classData)) {
            throw new IllegalArgumentException("Invalid classes claim.");
        }

        Object classId = classData.get("classId");
        Object role = classData.get("role");

        if (classId == null || role == null) {
            throw new IllegalArgumentException("Invalid classes claim.");
        }

        return new ContextClass(
                UUID.fromString(classId.toString()),
                ClassRole.valueOf(role.toString())
        );
    }
}
