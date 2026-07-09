package com.portal.conecta.hub.module.user.domain.exception;

/**
 * Lançada quando o usuário autenticado não tem permissão para executar a operação solicitada.
 * Mapeada para {@code 403 Forbidden} pelo {@link com.portal.conecta.hub.shared.exception.GlobalExceptionHandler}.
 */
public class UserPermissionDeniedException extends UserException {
    public UserPermissionDeniedException(String message) {
        super(message);
    }
}
