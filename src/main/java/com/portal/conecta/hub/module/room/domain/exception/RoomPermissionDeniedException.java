package com.portal.conecta.hub.module.room.domain.exception;

/**
 * Exceção lançada quando um usuário autenticado tenta realizar operações estruturais ou
 * administrativas no módulo de salas sem possuir o perfil de acesso exigido pelas regras.
 * <p>
 * O controle de privilégios avalia o nível do perfil em relação às permissões concedidas.
 * Esta exceção é mapeada para retornar o status HTTP 403 (Forbidden).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class RoomPermissionDeniedException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem padrão de rejeição por direitos insuficientes.
     */
    public RoomPermissionDeniedException() {
        super("Usuário não tem permissão para criar uma sala.");
    }
}
