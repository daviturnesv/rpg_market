package com.programacao_web.rpg_market.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex, HttpServletRequest request) {
        logger.warn("IOException occurred on request {}: {}", request.getRequestURI(), ex.getMessage());
        // Return empty response for broken pipe errors instead of error page
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex) {
        logger.warn("404 - No handler found: {}", ex.getRequestURL());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error on request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return "error/500";
    }
}
