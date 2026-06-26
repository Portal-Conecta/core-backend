package com.portal.conecta.hub.module.user.domain.exception;

/**
 * Lançada quando dados de usuário são inválidos ou obrigatórios estão ausentes.
 * Mapeada para {@code 400 Bad Request} pelo {@link com.portal.conecta.hub.shared.exception.GlobalExceptionHandler}.
 */
public class InvalidUserDataException extends UserException {

    public InvalidUserDataException(String message) {
        super(message);
    }
}
