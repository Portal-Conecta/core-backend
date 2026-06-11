package com.portal.conecta.hub.module.course.domain.exception;

public class CourseNameAlreadyInUseException extends RuntimeException {
    public CourseNameAlreadyInUseException(String nome) {
        super("Nome já está em uso: " + nome);
    }
}
