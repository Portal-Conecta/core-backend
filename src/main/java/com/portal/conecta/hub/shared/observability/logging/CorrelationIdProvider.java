package com.portal.conecta.hub.shared.observability.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Fornece o {@code correlationId} ativo no contexto de logging da requisição atual.
 *
 * <p>Lê o valor do MDC populado pelo {@link CorrelationIdFilter}. Quando não há
 * contexto ativo — como em threads de background ou testes isolados — retorna
 * um UUID novo para garantir rastreabilidade mínima do evento.</p>
 */
@Component
public class CorrelationIdProvider {

    /**
     * Retorna o {@code correlationId} do MDC atual, ou um UUID novo como fallback.
     *
     * @return correlationId da requisição corrente, ou UUID aleatório se ausente.
     */
    public String get() {
        String correlationId = MDC.get(LoggingContextKeys.CORRELATION_ID);
        return (correlationId != null && !correlationId.isBlank())
                ? correlationId
                : UUID.randomUUID().toString();
    }
}
