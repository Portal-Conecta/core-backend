package com.portal.conecta.hub.module.course.domain.exception;

public class CourseCodeAlreadyInUseException extends RuntimeException {
    public CourseCodeAlreadyInUseException(String message) {
        super(message);
    }
}
