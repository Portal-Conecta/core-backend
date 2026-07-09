package com.portal.conecta.hub.shared.observability.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.portal.conecta.logging.CorrelationIdResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CorrelationIdResolverTest {

    private static final String HEADER_NAME = "X-Correlation-Id";
    private static final int MAX_LENGTH = 128;

    private CorrelationIdResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CorrelationIdResolver();
    }

    @Test
    @DisplayName("deve preservar correlation ID valido recebido no header")
    void shouldPreserveValidCorrelationId() {
        String result = resolver.resolve("hub-test-123", HEADER_NAME, MAX_LENGTH);

        assertThat(result).isEqualTo("hub-test-123");
    }

    @Test
    @DisplayName("deve gerar UUID quando header esta ausente")
    void shouldGenerateUuidWhenHeaderIsAbsent() {
        String result = resolver.resolve(null, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header esta vazio")
    void shouldGenerateUuidWhenHeaderIsBlank() {
        String result = resolver.resolve("   ", HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header tem caractere invalido")
    void shouldGenerateUuidWhenHeaderHasInvalidCharacter() {
        String result = resolver.resolve("invalid id!", HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve gerar UUID quando header ultrapassa o limite de 128 caracteres")
    void shouldGenerateUuidWhenHeaderExceedsMaxLength() {
        String result = resolver.resolve("a".repeat(129), HEADER_NAME, MAX_LENGTH);

        assertThat(result).isNotBlank();
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    @Test
    @DisplayName("deve preservar correlation ID com exatamente 128 caracteres")
    void shouldPreserveCorrelationIdWithExactlyMaxLength() {
        String value = "a".repeat(128);

        String result = resolver.resolve(value, HEADER_NAME, MAX_LENGTH);

        assertThat(result).isEqualTo(value);
    }
}
