package com.portal.conecta.hub.module.auth.domain.exception;

/**
 * Falha de autenticação por credenciais inválidas ou token JWT inválido/expirado.
 * Mapeada para HTTP 401.
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

}