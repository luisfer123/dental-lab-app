package com.dentallab.api.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dentallab.api.dto.ErrorResponse;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PricingExceptionHandler {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PricingExceptionHandler.class);

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
    	logger.debug("Illegal state encountered: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)	
                .body(new ErrorResponse(
                        "pricing_not_fixed",
                        ex.getMessage()
                ));
    }
}
