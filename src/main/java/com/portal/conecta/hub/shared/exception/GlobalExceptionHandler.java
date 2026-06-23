package com.portal.conecta.hub.shared.exception;

import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.InvalidRefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.classes.domain.exception.*;
import com.portal.conecta.hub.module.course.domain.exception.*;
import com.portal.conecta.hub.module.room.domain.exception.InvalidRoomDataException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNotFoundException;
import com.portal.conecta.hub.module.room.domain.exception.RoomNumberAlreadyInUseException;
import com.portal.conecta.hub.module.room.domain.exception.RoomPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String USER_EMAIL_CONSTRAINT = "uk_users_email";
    private static final String COURSE_NAME_CONSTRAINT = "uk_courses_name";
    private static final String COURSE_CODE_CONSTRAINT = "uk_courses_code";

    @ExceptionHandler({
            UnauthorizedUserException.class,
            AuthException.class,
            InvalidRefreshTokenException.class
    })
    public ResponseEntity<ApiError> handleUnauthorized(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiError> handleRefreshTokenException(
            RefreshTokenException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler({
            UserPermissionDeniedException.class,
            RoomPermissionDeniedException.class
    })
    public ResponseEntity<ApiError> handleForbidden(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler({
            InvalidUserDataException.class,
            InvalidRoomDataException.class,
            InvalidClassDataException.class,
            InvalidCourseDataException.class,
            ClassMembershipException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler({
            UserNotFoundException.class,
            CourseNotFoundException.class,
            DeletedCourseException.class,
            ClassEntityNotFoundException.class,
            ClassMembershipNotFoundException.class,
            RoomNotFoundException.class
    })
    public ResponseEntity<ApiError> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler({
            EmailAlreadyInUseException.class,
            CourseCodeAlreadyInUseException.class,
            CourseNameAlreadyInUseException.class,
            RoomNumberAlreadyInUseException.class,
            ClassNumberAlreadyInUseException.class
    })
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        String constraintName = extractConstraintName(exception);

        if (USER_EMAIL_CONSTRAINT.equals(constraintName)) {
            return buildResponse(HttpStatus.CONFLICT, "E-mail já está em uso.", request);
        }

        if (COURSE_NAME_CONSTRAINT.equals(constraintName)) {
            return buildResponse(HttpStatus.CONFLICT, "O nome do curso já está em uso.", request);
        }

        if (COURSE_CODE_CONSTRAINT.equals(constraintName)) {
            return buildResponse(HttpStatus.CONFLICT, "O código do curso já está em uso.", request);
        }

        log.warn("Violação de integridade de dados sem constraint mapeada. Constraint: {}", constraintName, exception);

        if (constraintName != null && constraintName.startsWith("uk_")) {
            return buildResponse(HttpStatus.CONFLICT, "O recurso já existe.", request);
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Violação de integridade de dados.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBind(
            BindException exception,
            HttpServletRequest request
    ) {
        return buildValidationResponse(exception.getBindingResult().getFieldErrors(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ApiError.FieldErrorDetail> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();

                    if (field.contains(".")) {
                        field = field.substring(field.lastIndexOf('.') + 1);
                    }

                    return new ApiError.FieldErrorDetail(
                            field,
                            violation.getMessage()
                    );
                })
                .toList();

        String message = errors.stream()
                .map(ApiError.FieldErrorDetail::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Requisição inválida.");

        log.warn("Falha de validação (Constraint): Path={}, Erros={}", path(request), errors);

        return ResponseEntity
                .badRequest()
                .body(ApiError.validation(
                        HttpStatus.BAD_REQUEST,
                        message,
                        path(request),
                        errors
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String message = "Valor inválido para o parâmetro '%s'.".formatted(exception.getName());

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        String message = "O parâmetro obrigatório '%s' está ausente.".formatted(exception.getParameterName());

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.debug("Corpo da requisição inválido.", exception);

        return buildResponse(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        log.error("Exceção de Runtime interceptada: ", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Erro inesperado ocorreu: ", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado.",
                request
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(status, exception.getMessage(), request);
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        if (status.is4xxClientError()) {
            log.warn("Exceção de negócio/cliente interceptada: Status={}, Mensagem='{}', Path={}",
                    status.value(), message, path(request));
        }

        return ResponseEntity
                .status(status)
                .body(ApiError.of(status, message, path(request)));
    }

    private ResponseEntity<ApiError> buildValidationResponse(
            List<FieldError> fieldErrors,
            HttpServletRequest request
    ) {
        List<ApiError.FieldErrorDetail> errors = fieldErrors.stream()
                .map(fieldError -> new ApiError.FieldErrorDetail(
                        fieldError.getField(),
                        Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Valor inválido.")
                ))
                .toList();

        String message = errors.stream()
                .map(ApiError.FieldErrorDetail::message)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Requisição inválida.");

        log.warn("Falha de validação de DTO: Path={}, Erros={}", path(request), errors);

        return ResponseEntity
                .badRequest()
                .body(ApiError.validation(
                        HttpStatus.BAD_REQUEST,
                        message,
                        path(request),
                        errors
                ));
    }

    private String extractConstraintName(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException constraintViolationException) {
                return constraintViolationException.getConstraintName();
            }

            current = current.getCause();
        }

        return null;
    }

    private String path(HttpServletRequest request) {
        return request.getRequestURI();
    }
}