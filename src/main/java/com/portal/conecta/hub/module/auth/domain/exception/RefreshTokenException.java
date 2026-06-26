package com.portal.conecta.hub.module.auth.domain.exception;

/**
 * Usuário inativo, bloqueado ou não encontrado durante o fluxo de refresh.
 * Mapeada para HTTP 403.
 */
public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String message) {
        super(message);
    }
}
