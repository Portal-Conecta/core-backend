package com.portal.conecta.hub.module.course.domain.exception;

/**
 * Exceção lançada quando uma operação solicita um curso através do seu ID,
 * mas o registro correspondente não é encontrado no banco de dados.
 * <p>
 * Esta exceção também pode ser lançada se o curso existir fisicamente, mas possuir
 * o status de exclusão lógica (soft delete). O handler global captura esta exceção
 * e retorna o status HTTP 404 (Not Found).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class CourseNotFoundException extends RuntimeException {

    /**
     * Constrói a exceção com a mensagem padrão indicando que o recurso não foi localizado.
     */
    public CourseNotFoundException() {
        super("Curso não encontrado.");
    }
}
