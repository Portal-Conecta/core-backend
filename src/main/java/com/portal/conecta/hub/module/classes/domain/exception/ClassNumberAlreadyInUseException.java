package com.portal.conecta.hub.module.classes.domain.exception;

public class ClassNumberAlreadyInUseException extends RuntimeException {
  public ClassNumberAlreadyInUseException(int number) {
    super("Já existe uma turma com o número " + number + " para este curso.");
  }
}
