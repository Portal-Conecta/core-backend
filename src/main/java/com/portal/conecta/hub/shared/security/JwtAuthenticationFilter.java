package com.portal.conecta.hub.shared.security;

import com.portal.conecta.hub.shared.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LOGIN_PATH = "/auth/login";

    private final JwtExtractToken jwtExtractToken;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public JwtAuthenticationFilter(
            JwtExtractToken jwtExtractToken,
            SecurityErrorResponseWriter securityErrorResponseWriter
    ) {
        this.jwtExtractToken = jwtExtractToken;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                && LOGIN_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            reject(response, "Missing or incorrectly formatted token");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            if (!jwtExtractToken.isValidToken(token)) {
                reject(response, "Invalid or expired token");
                return;
            }

            CustomUserDetails userDetails = jwtExtractToken.extractUserDetails(token);
            RequestContext requestContext = userDetails.toRequestContext();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            requestContext,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            reject(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        securityErrorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
