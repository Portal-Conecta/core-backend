package com.portal.conecta.hub.module.classes.domain.exception;

/**
 * Lançada quando já existe uma turma ativa com o mesmo número no curso informado.
 *
 * <p>O número da turma deve ser único por curso entre as turmas não removidas.</p>
 */
public class ClassNumberAlreadyInUseException extends RuntimeException {
  public ClassNumberAlreadyInUseException(int number) {
    super("Já existe uma turma com o número " + number + " para este curso.");
  }
}
