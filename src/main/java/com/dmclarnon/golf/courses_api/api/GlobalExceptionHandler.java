package com.dmclarnon.golf.courses_api.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 (thrown in your service with NoSuchElementException)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req, null);
    }

    // 403 (ownership / scope failures)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), req, null);
    }

    // 400 – bean validation on @RequestBody DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("fieldErrors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "rejectedValue", fe.getRejectedValue(),
                        "message", readable(fe)))
                .toList());
        return build(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid", req, details);
    }

    // 400 – @RequestParam / path variable validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, Object> details = Map.of(
                "violations", ex.getConstraintViolations().stream()
                        .map(v -> Map.of(
                                "property", v.getPropertyPath().toString(),
                                "invalidValue", String.valueOf(v.getInvalidValue()),
                                "message", v.getMessage()))
                        .toList());
        return build(HttpStatus.BAD_REQUEST, "Constraint Violation", "Invalid request parameters", req, details);
    }

    // 400 – bad JSON, enum parse, etc.
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, null);
    }

    // 409 – unique constraints, FK violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Data Integrity Violation", cleanSqlMessage(ex.getMostSpecificCause().getMessage()), req, null);
    }

    // 500 – fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), req, null);
    }

    // ---------- helpers ----------

    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message,
                                           HttpServletRequest req, Map<String, Object> details) {
        ApiError body = ApiError.of(status.value(), error, message, req.getRequestURI(), details);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private String readable(FieldError fe) {
        // Prefer annotation message if present, else default
        return Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid value");
    }

    private String cleanSqlMessage(String msg) {
        if (msg == null) return null;
        // Trim noisy driver prefixes
        return msg.replaceAll("\\n", " ").replaceAll("\\s+", " ").trim();
    }
}
