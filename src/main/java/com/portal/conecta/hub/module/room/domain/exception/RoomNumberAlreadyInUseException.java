package com.portal.conecta.hub.module.room.domain.exception;

/**
 * Exceção lançada quando há uma tentativa de criar ou atualizar uma sala utilizando um
 * número de identificação física que já está registrado e em uso por outra sala ativa.
 * <p>
 * O número da sala representa uma restrição de unicidade no domínio. Esta exceção é mapeada
 * para retornar o status HTTP 409 (Conflict) no tratamento global do ecossistema.
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class RoomNumberAlreadyInUseException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem formatada contendo o número em conflito.
     *
     * @param number O número da sala que violou a regra de unicidade de negócio.
     */
    public RoomNumberAlreadyInUseException(Integer number) {
        super("O número da sala '" + number + "' já está em uso.");
    }
}
