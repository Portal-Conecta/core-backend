package com.portal.conecta.hub.module.course.domain.exception;

/**
 * Exceção lançada quando há uma tentativa de criar ou atualizar um curso
 * utilizando um código que já está registrado no banco de dados para outro curso ativo.
 * <p>
 * O código do curso é uma chave de negócio única (constraint {@code uk_courses_code}).
 * Esta exceção é mapeada para retornar o status HTTP 409 (Conflict).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class CourseCodeAlreadyInUseException extends RuntimeException {

    /**
     * Constrói a exceção com uma mensagem padronizada contendo o código em conflito.
     *
     * @param codigo O código do curso que violou a regra de unicidade.
     */
    public CourseCodeAlreadyInUseException(String codigo) {
        super("Código já está em uso: " + codigo);
    }
}
