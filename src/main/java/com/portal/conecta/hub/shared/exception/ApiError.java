package com.portal.conecta.hub.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Estrutura padrão de resposta de erro do Hub Core.
 *
 * <p>Todos os erros HTTP retornados pela API seguem este contrato.
 * O campo {@code errors} é omitido quando nulo — presente apenas em erros de validação.
 *
 * <p>Instâncias são criadas exclusivamente pelos factory methods {@link #of} e {@link #validation}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> errors
) {

    /**
     * Cria um erro simples sem detalhes de campo.
     */
    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    /**
     * Cria um erro de validação com detalhes por campo.
     */
    public static ApiError validation(
            HttpStatus status,
            String message,
            String path,
            List<FieldErrorDetail> errors
    ) {
        return new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                errors
        );
    }

    public record FieldErrorDetail(
            String field,
            String message
    ) {
    }
}