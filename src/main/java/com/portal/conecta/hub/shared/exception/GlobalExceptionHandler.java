package com.portal.conecta.hub.shared.exception;

import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

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

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, RuntimeException exception) {
        return ResponseEntity.status(status).body(new ApiError(exception.getMessage()));
    }

    private record ApiError(String message) {
    }
}
