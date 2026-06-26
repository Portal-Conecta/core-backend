package com.portal.conecta.hub.module.room.domain.exception;

/**
 * Exceção lançada quando uma sala física solicitada por meio de seu identificador único (UUID)
 * não é localizada na base de dados, ou encontra-se em estado de exclusão lógica.
 * <p>
 * Esta exceção é mapeada para retornar o status HTTP 404 (Not Found).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class RoomNotFoundException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem padronizada indicando a ausência do registro.
     */
    public RoomNotFoundException() {
        super("Sala não encontrada.");
    }
}
