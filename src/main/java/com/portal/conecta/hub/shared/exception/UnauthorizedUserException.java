package com.portal.conecta.hub.shared.exception;

/**
 * Lançada quando a requisição exige autenticação e ela está ausente ou inválida.
 * Mapeada para {@code 401 Unauthorized} pelo {@link GlobalExceptionHandler}.
 */
public class UnauthorizedUserException extends RuntimeException {

  public UnauthorizedUserException(String message) {
    super(message);
  }

  public UnauthorizedUserException() {
    super("Usuário não autorizado.");
  }
}