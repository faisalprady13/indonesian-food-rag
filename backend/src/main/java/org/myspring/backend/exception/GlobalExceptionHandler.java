package org.myspring.backend.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach((fieldError) -> validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handeUserNotFound(UserNotFound except) {
        return "Error: " + except.getLocalizedMessage();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleEmailAlreadyExists(EmailAlreadyExistsException except) {
        return except.getLocalizedMessage();
    }

}
