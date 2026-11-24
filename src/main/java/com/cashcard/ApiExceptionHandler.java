package com.cashcard;

import exceptions.CashCardNotFoundExcpetion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(CashCardNotFoundExcpetion.class)
    public ResponseEntity<Map<String,String>> handleNotFound(CashCardNotFoundExcpetion ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Method called when validation for attributes/ columns fails.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValid(MethodArgumentNotValidException ex){
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse body = new ErrorResponse(
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        ErrorResponse body = new ErrorResponse(
                "Malformed JSON or invalid field type",
                HttpStatus.BAD_REQUEST.value(),
                Map.of("error", ex.getMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
