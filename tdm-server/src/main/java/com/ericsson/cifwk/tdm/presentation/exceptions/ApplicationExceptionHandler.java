package com.ericsson.cifwk.tdm.presentation.exceptions;

import com.ericsson.cifwk.tdm.api.model.validation.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.LOCKED;

@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
public class ApplicationExceptionHandler {

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationError handle(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        return processFieldErrors(fieldErrors);
    }

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public List<String> handle(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }

    @ResponseBody
    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(LockException.class)
    public ErrorMessage handle(LockException e) {
        return new ErrorMessage(e.getMessage());
    }

    private static ValidationError processFieldErrors(List<FieldError> fieldErrors) {
        ValidationError error = new ValidationError();
        for (FieldError fieldError : fieldErrors) {
            error.addFieldError(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return error;
    }

    @ResponseBody
    @ResponseStatus(value = BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorMessage handle(IllegalArgumentException e) {
        return new ErrorMessage(e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(value = LOCKED)
    @ExceptionHandler(LockedException.class)
    public String handle(LockedException e) {
        return e.getMessage();
    }

}
