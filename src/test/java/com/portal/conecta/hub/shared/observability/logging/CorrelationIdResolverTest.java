package com.portal.conecta.hub.shared.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

class CorrelationIdResolverTest {

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final int MAX_LENGTH = 128;

    private CorrelationIdResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CorrelationIdResolver();
    }

    @Test
    @DisplayName("deve preservar correlation ID válido recebido no header")
    void shouldPreserveValidCorrelationId() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("hub-test-123");

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isEqualTo("hub-test-123");
    }

    @Test
    @DisplayName("deve gerar UUID quando header está ausente")
    void shouldGenerateUuidWhenHeaderIsAbsent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn(null);

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header está vazio")
    void shouldGenerateUuidWhenHeaderIsBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("   ");

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header tem caractere inválido")
    void shouldGenerateUuidWhenHeaderHasInvalidCharacter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("invalid id!");

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header ultrapassa o limite de 128 caracteres")
    void shouldGenerateUuidWhenHeaderExceedsMaxLength() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("a".repeat(129));

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve preservar correlation ID com exatamente 128 caracteres")
    void shouldPreserveCorrelationIdWithExactlyMaxLength() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String value = "a".repeat(128);
        when(request.getHeader(HEADER_NAME)).thenReturn(value);

        String result = resolver.resolve(request, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isEqualTo(value);
    }
}