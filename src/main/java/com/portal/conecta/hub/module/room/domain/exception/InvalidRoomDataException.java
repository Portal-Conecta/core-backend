package com.portal.conecta.hub.module.room.domain.exception;

/**
 * Exceção lançada quando os dados fornecidos para uma operação com salas físicas violam regras
 * de consistência ou o estado atual da entidade.
 * <p>
 * Cenários comuns incluem: tentativa de edição em salas excluídas, remoção de uma sala que já
 * foi excluída, restauração de uma sala ativa, ou envio de comandos de atualização vazios.
 * Esta exceção é mapeada para retornar o status HTTP 400 (Bad Request).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class InvalidRoomDataException extends RuntimeException{

    /**
     * Constrói a exceção com uma mensagem detalhada descrevendo a inconsistência dos dados.
     *
     * @param message Mensagem explicativa da violação de consistência.
     */
    public InvalidRoomDataException(String message) {
        super(message);
    }
}
