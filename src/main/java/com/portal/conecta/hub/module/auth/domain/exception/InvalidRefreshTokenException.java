package com.portal.conecta.hub.module.auth.domain.exception;

/**
 * Refresh token não encontrado na base — revogado, rotacionado ou inexistente.
 *
 * <p>Difere de {@link AuthException}, que cobre falhas de assinatura/expiração JWT.
 * Aqui o token é estruturalmente válido mas não está mais persistido.</p>
 *
 * <p>Mapeada para HTTP 401.</p>
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}