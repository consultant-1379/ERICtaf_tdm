package com.ericsson.cifwk.tdm.presentation.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
public class DefaultExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    public void handleAll(Throwable throwable) {
        LOGGER.error(throwable.getMessage(), throwable);
        throw new RuntimeException(throwable);
    }

}
