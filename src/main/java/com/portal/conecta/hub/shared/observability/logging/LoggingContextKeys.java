package com.portal.conecta.hub.shared.observability.logging;

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