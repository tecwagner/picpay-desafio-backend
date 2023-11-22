package com.picpay.utils;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.picpay.dtos.ExceptionDTO;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class ErrorExceptionHandler {
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity threatDuplicateEntry(DataIntegrityViolationException exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO("Usuário já está cadastrado.", "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity threat404(EntityNotFoundException exception){
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity threatGenerateException(Exception exception){
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "500");
        return ResponseEntity.internalServerError().body(exceptionDTO);
    }

}
