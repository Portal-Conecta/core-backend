package com.portal.conecta.hub.shared.exception;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.course.domain.exception.*;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedUserException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedUserException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String message = ex.getMessage();

        if (message != null && message.toLowerCase().contains("rooms") && message.toLowerCase().contains("number")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiError.of(
                            HttpStatus.CONFLICT,
                            "Room number is already in use.",
                            path(request)
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(
                        HttpStatus.BAD_REQUEST,
                        "Data integrity violation.",
                        path(request)
                ));
    }

    @ExceptionHandler(UserPermissionDeniedException.class)
    public ResponseEntity<ApiError> handleUserPermissionDenied(
            UserPermissionDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ApiError> handleInvalidUserData(
            InvalidUserDataException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyInUse(
            EmailAlreadyInUseException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(BindException exception, HttpServletRequest request) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn("Invalid request body.", exception);
        return ResponseEntity.badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, "Invalid request body.", path(request)));
    }

    @ExceptionHandler(ClassMembershipException.class)
    public ResponseEntity<ApiError> handleClassMembership(
            ClassMembershipException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST,exception,request);
    }

    @ExceptionHandler(RoomPermissionDeniedException.class)
    public ResponseEntity<ApiError> handleRoomPermissionDenied(
            RoomPermissionDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(InvalidRoomDataException.class)
    public ResponseEntity<ApiError> handleInvalidRoomData(
            InvalidRoomDataException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(RoomNumberAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleRoomNumberAlreadyInUse(
            RoomNumberAlreadyInUseException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiError> handleAuthException(AuthException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }

    @ExceptionHandler(InvalidClassDataException.class)
    public ResponseEntity<ApiError> handleInvalidClassData(
            InvalidClassDataException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ApiError> handleCourseNotFound(CourseNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(status).body(ApiError.of(status, exception.getMessage(), path(request)));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException exception, HttpServletRequest request) {
        log.error("Runtime exception intercepted: ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", path(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error occurred: ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", path(request)));
    }


    @ExceptionHandler(ClassEntityNotFoundException.class)
    public ResponseEntity<ApiError> handleClassNotFound(ClassEntityNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    private ResponseEntity<ApiError> buildValidationResponse(List<FieldError> fieldErrors, HttpServletRequest request) {
        String message = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Invalid request.");

        return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, message, path(request)));
    }

    private String path(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ExceptionHandler(CourseCodeAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleCourseCodeAlreadyInUse(CourseCodeAlreadyInUseException courseCodeAlreadyInUseException, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, courseCodeAlreadyInUseException, request);
    }

    @ExceptionHandler(CourseNameAlreadyInUseException.class)
    public ResponseEntity<ApiError> handleCourseNameAlreadyInUse(CourseNameAlreadyInUseException courseNameAlreadyInUseException, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, courseNameAlreadyInUseException, request);
    }

    @ExceptionHandler(DeletedCourseException.class)
    public ResponseEntity<ApiError> handleDeletedCourse(DeletedCourseException deletedCourseException, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, deletedCourseException, request);
    }

    @ExceptionHandler(InvalidCourseDataException.class)
    public ResponseEntity<ApiError> handleInvalidCourseData(InvalidCourseDataException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(CourseEntityNotFoundException.class)
    public ResponseEntity<ApiError> handleModuleCourseNotFound(CourseEntityNotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }



}