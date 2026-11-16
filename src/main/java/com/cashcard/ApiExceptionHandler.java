package com.cashcard;

import exceptions.CashCardNotFoundExcpetion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(CashCardNotFoundExcpetion.class)
    public ResponseEntity<Void> handleNotFound(){
        return ResponseEntity.notFound().build();
    }
}
