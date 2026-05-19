package com.portal.conecta.hub.shared.security;

import com.portal.conecta.hub.shared.context.ContextClass;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtExtractToken {

    @Value("${app.jwt.secret}")
    private String secret;

    public CustomUserDetails extractUserDetails(String token){
        Claims claims = extractClaims(token);

        String userId = claims.getSubject();
        String userType = claims.get("userType", String.class);
        String permissionVersion = claims.get("permissionVersion", String.class);
        List<ContextClass> classes = claims.get("classes", List.class);

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

}
