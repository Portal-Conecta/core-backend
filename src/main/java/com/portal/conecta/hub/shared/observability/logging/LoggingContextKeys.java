package com.portal.conecta.hub.shared.observability.logging;

/**
 * Constantes das chaves MDC utilizadas nos filtros de observabilidade.
 *
 * <p>Centraliza os nomes para evitar divergência entre {@link CorrelationIdFilter},
 * {@link AccessLogFilter} e qualquer configuração de appender estruturado (ex: Logstash).
 */
public final class LoggingContextKeys {

    public static final String CORRELATION_ID = "correlationId";
    public static final String USER_ID        = "userId";
    public static final String METHOD         = "method";
    public static final String PATH           = "path";
    public static final String STATUS         = "status";
    public static final String DURATION_MS    = "durationMs";

    private LoggingContextKeys() {
    }

}