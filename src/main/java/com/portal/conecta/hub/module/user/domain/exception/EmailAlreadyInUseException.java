package com.portal.conecta.hub.module.user.domain.exception;

/**
 * Lançada quando o e-mail informado já está em uso por outro usuário.
 * Mapeada para {@code 409 Conflict} pelo {@link com.portal.conecta.hub.shared.exception.GlobalExceptionHandler}.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String email) {
        super("E-mail já está em uso: " +email);
    }
}
