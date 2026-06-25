package com.portal.conecta.hub.shared.observability.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portal.observability.logging")
public class LoggingProperties {

    private String correlationHeader = "X-Correlation-Id";
    private int maxCorrelationIdLength = 128;
    private boolean accessLogEnabled = true;

    public LoggingProperties(String correlationHeader, int maxCorrelationIdLength, boolean accessLogEnabled) {
        this.correlationHeader = correlationHeader;
        this.maxCorrelationIdLength = maxCorrelationIdLength;
        this.accessLogEnabled = accessLogEnabled;
    }

    public String getCorrelationHeader() {
        return correlationHeader;
    }

    public void setCorrelationHeader(String correlationHeader) {
        this.correlationHeader = correlationHeader;
    }

    public int getMaxCorrelationIdLength() {
        return maxCorrelationIdLength;
    }

    public void setMaxCorrelationIdLength(int maxCorrelationIdLength) {
        this.maxCorrelationIdLength = maxCorrelationIdLength;
    }

    public boolean isAccessLogEnabled() {
        return accessLogEnabled;
    }

    public void setAccessLogEnabled(boolean accessLogEnabled) {
        this.accessLogEnabled = accessLogEnabled;
    }

}