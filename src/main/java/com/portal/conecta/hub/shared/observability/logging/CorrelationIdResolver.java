package com.portal.conecta.hub.shared.observability.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationIdResolver {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Za-z0-9._:-]+$");

    public String resolve(HttpServletRequest request, String headerName, int maxLength) {
        String candidate = request.getHeader(headerName);

        if (isValid(candidate, maxLength)) {
            return candidate;
        }

        return UUID.randomUUID().toString();
    }

    private boolean isValid(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return false;
        }

        if (value.length() > maxLength) {
            return false;
        }

        return VALID_PATTERN.matcher(value).matches();
    }

}