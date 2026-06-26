package com.portal.conecta.hub.module.course.domain.exception;

/**
 * Exceção genérica de domínio lançada quando os dados fornecidos para a entidade de Curso
 * ferem alguma regra de negócio ou formatação interna.
 * <p>
 * Trata cenários como valores nulos não permitidos, limitações de tamanho de texto ou dados inconsistentes
 * que não são cobertos por anotações de validação padrão (como @NotNull).
 * É capturada pelo handler global e retorna um status HTTP 400 (Bad Request).
 * </p>
 *
 * @see com.portal.conecta.hub.shared.exception.GlobalExceptionHandler
 */
public class InvalidCourseDataException extends RuntimeException {

    /**
     * Constrói a exceção com a especificação exata de qual dado do curso estava inválido.
     *
     * @param message A descrição detalhada do problema de validação.
     */
    public InvalidCourseDataException(String message) {
        super(message);
    }
}
