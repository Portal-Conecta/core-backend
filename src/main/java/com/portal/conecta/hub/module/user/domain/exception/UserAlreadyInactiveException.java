package com.portal.conecta.hub.module.user.domain.exception;

/**
 * Lançada quando se tenta desativar um usuário que já está inativo.
 * Mapeada para {@code 400 Bad Request} pelo {@link com.portal.conecta.hub.shared.exception.GlobalExceptionHandler}.
 */
public class UserAlreadyInactiveException extends RuntimeException {

    public UserAlreadyInactiveException () {super ("Usuário já está inativo.");}
}
