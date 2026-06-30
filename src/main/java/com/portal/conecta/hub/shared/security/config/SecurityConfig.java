package com.portal.conecta.hub.shared.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.portal.conecta.hub.shared.security.error.SecurityErrorResponseWriter;
import com.portal.conecta.hub.shared.security.filter.JwtAuthenticationFilter;

/**
 * Configura a cadeia de filtros de segurança do Hub Core.
 *
 * <p>Política adotada: stateless (sem sessão), sem CSRF, sem form login e sem HTTP Basic.
 * O {@link JwtAuthenticationFilter} é inserido antes do filtro padrão do Spring Security.
 *
 * <p>Rotas públicas: {@code POST /auth/login}, {@code POST /auth/refresh},
 * documentação Swagger/OpenAPI e endpoints de saúde e métricas do Actuator.
 * Todas as demais requisições exigem autenticação.
 *
 * <p>Erros de autenticação e autorização são escritos como {@link com.portal.conecta.hub.shared.exception.ApiError}
 * via {@link SecurityErrorResponseWriter}, retornando {@code 401} e {@code 403} respectivamente.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            SecurityErrorResponseWriter securityErrorResponseWriter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                securityErrorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Autenticação é obrigatória."
                                ))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                securityErrorResponseWriter.write(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acesso negado."
                                ))
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
