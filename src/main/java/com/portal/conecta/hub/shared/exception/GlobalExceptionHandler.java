package com.portal.conecta.hub.shared.exception;

import com.portal.conecta.hub.module.classes.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedUserException exception) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception);
    }

    @ExceptionHandler(UserPermissionDeniedException.class)
    public ResponseEntity<ApiError> handleUserPermissionDenied(UserPermissionDeniedException exception) {
        return buildResponse(HttpStatus.FORBIDDEN, exception);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ApiError> handleInvalidUserData(InvalidUserDataException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(EmailAlreadyInUseException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(BindException exception) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        LOGGER.warn("Invalid request body.", exception);
        return ResponseEntity.badRequest().body(new ApiError("Invalid request body."));
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(new ApiError(exception.getMessage()));
    }

    private ResponseEntity<ApiError> buildValidationResponse(List<FieldError> fieldErrors) {
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Invalid request.");

        return ResponseEntity.badRequest().body(new ApiError(message));
    }

    private record ApiError(String message) {
    }

    @ExceptionHandler(InvalidClassDataException.class)
    public ResponseEntity<ApiError> handleInvalidClassData(InvalidClassDataException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ApiError> handleCourseNotFound(CourseNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }
}
