package com.portal.conecta.hub.module.course.domain.exception;

/**
 * Exceção lançada ao tentar realizar uma ação não permitida em um curso que já
 * sofreu exclusão lógica (marcado com {@code deletedAt}).
 * <p>
 * Geralmente acionada em regras de negócio que impedem a atualização ou manipulação de entidades inativas.
 * Esta exceção resulta em um status HTTP 404 (Not Found) tratado pelo handler global.
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class DeletedCourseException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem customizada sobre a falha de operação no curso deletado.
     *
     * @param message A descrição detalhada do erro.
     */
    public DeletedCourseException(String message) {
        super(message);
    }
}
