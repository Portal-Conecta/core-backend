package com.portal.conecta.hub.module.user.domain.exception;

/**
 * Lançada quando o usuário solicitado não existe, está inativo ou foi excluído.
 * Mapeada para {@code 404 Not Found} pelo {@link com.portal.conecta.hub.shared.exception.GlobalExceptionHandler}.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException() {
        super("Usuário não encontrado.");
    }
}
