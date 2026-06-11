package com.portal.conecta.hub.module.course.domain.exception;

public class CourseCodeAlreadyInUseException extends RuntimeException {

    public CourseCodeAlreadyInUseException(String codigo) {
        super("Código já está em uso: " + codigo);
    }
}
