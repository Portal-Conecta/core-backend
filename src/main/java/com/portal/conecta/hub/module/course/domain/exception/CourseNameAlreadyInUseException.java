package com.portal.conecta.hub.module.course.domain.exception;

public class CourseNameAlreadyInUseException extends RuntimeException {
    public CourseNameAlreadyInUseException(String message) {
        super(message);
    }
}
