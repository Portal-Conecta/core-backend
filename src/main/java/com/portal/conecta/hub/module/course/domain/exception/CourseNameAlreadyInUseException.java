package com.portal.conecta.hub.module.course.domain.exception;

/**
 * Exceção lançada quando há uma tentativa de criar ou atualizar um curso
 * utilizando um nome que já pertence a outro curso no sistema.
 * <p>
 * O nome do curso deve ser único (constraint {@code uk_courses_name}) para evitar ambiguidades no catálogo.
 * Esta exceção é tratada pelo handler global para retornar o status HTTP 409 (Conflict).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class CourseNameAlreadyInUseException extends RuntimeException {

    /**
     * Constrói a exceção informando o nome que gerou o conflito.
     *
     * @param nome O nome do curso que violou a regra de unicidade.
     */
    public CourseNameAlreadyInUseException(String nome) {
        super("Nome já está em uso: " + nome);
    }
}
