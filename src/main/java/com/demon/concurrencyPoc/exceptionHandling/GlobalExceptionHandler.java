package com.demon.concurrencyPoc.exceptionHandling;

import com.demon.concurrencyPoc.exceptionHandling.customExceptions.ConcurrentUpdateException;
import com.demon.concurrencyPoc.exceptionHandling.customExceptions.UserNotFoundException;
import com.demon.concurrencyPoc.exceptionHandling.dto.ApiError;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ApiError buildError(HttpStatus status, String message, String path) {
        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(
                buildError(status, ex.getMessage(), request.getRequestURI()),
                status
        );
    }

    @ExceptionHandler({ConcurrentUpdateException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> handleConcurrentUpdate(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.CONFLICT; // 409
        String message = "Concurrent update detected: " + ex.getMessage();
        return new ResponseEntity<>(
                buildError(status, message, request.getRequestURI()),
                status
        );
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiError> handleRateLimit(
            RequestNotPermitted ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS; // 429
        String message = "Too many requests - please slow down.";
        return new ResponseEntity<>(
                buildError(status, message, request.getRequestURI()),
                status
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>(
                buildError(status, message, request.getRequestURI()),
                status
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        // For POC you can log stacktrace here with SLF4J
        return new ResponseEntity<>(
                buildError(status, ex.getMessage(), request.getRequestURI()),
                status
        );
    }
}