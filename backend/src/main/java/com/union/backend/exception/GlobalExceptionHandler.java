package com.union.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidUnionCardException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUnionCard(InvalidUnionCardException exception){
        ErrorResponse incorrectData = new ErrorResponse();
        incorrectData.setInfo(exception.getMessage());
        return new ResponseEntity<>(incorrectData,HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStudentFind(StudentNotFoundException exception){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setInfo(exception.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(ApplicationPeriodClosedException.class)
    public ResponseEntity<ErrorResponse> applicationPeriodClosedException(ApplicationPeriodClosedException exception){
        ErrorResponse errorsResponse = new ErrorResponse();
        errorsResponse.setInfo(exception.getMessage());
        return new ResponseEntity<>(errorsResponse,HttpStatus.CONFLICT);
    }
}
