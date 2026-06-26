package com.portal.conecta.hub.shared.observability.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de access log que registra método, path, status e duração de cada requisição HTTP.
 *
 * <p>Executado com {@code @Order(-90)}, após {@link CorrelationIdFilter}.
 * Popula o MDC com as chaves definidas em {@link LoggingContextKeys} e as remove ao final,
 * garantindo que não vazem entre requisições.
 *
 * <p>Inclui o {@code userId} do usuário autenticado quando disponível no contexto de segurança.
 * Pode ser desativado via {@code portal.observability.logging.access-log-enabled=false}.
 *
 * <p>O método {@link #afterLog()} é um hook vazio destinado a extensão em testes.
 */
@Component
@Order(-90)
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    private final AuthenticatedUserLogResolver userLogResolver;
    private final LoggingProperties loggingProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!loggingProperties.isAccessLogEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;

            MDC.put(LoggingContextKeys.METHOD, request.getMethod());
            MDC.put(LoggingContextKeys.PATH, request.getRequestURI());
            MDC.put(LoggingContextKeys.STATUS, String.valueOf(response.getStatus()));
            MDC.put(LoggingContextKeys.DURATION_MS, String.valueOf(durationMs));

            userLogResolver.resolve()
                    .ifPresent(userId -> MDC.put(LoggingContextKeys.USER_ID, userId));

            log.info("Requisição HTTP concluída.");

            afterLog();

            MDC.remove(LoggingContextKeys.METHOD);
            MDC.remove(LoggingContextKeys.PATH);
            MDC.remove(LoggingContextKeys.STATUS);
            MDC.remove(LoggingContextKeys.DURATION_MS);
            MDC.remove(LoggingContextKeys.USER_ID);
        }
    }

    protected void afterLog() {
    }

}