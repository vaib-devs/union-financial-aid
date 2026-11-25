package com.union.backend.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> methodArgumentNotValidException(MethodArgumentNotValidException exception){
        ValidationErrorResponse validationErrorResponse =
    new ValidationErrorResponse();
        List<String> messages = new ArrayList<>();
        ErrorResponse errorsResponse = new ErrorResponse();
        List<FieldError> error = exception.getBindingResult().getFieldErrors();
        if (!error.isEmpty()){
            for (FieldError e:error){
                messages.add(e.getDefaultMessage());
            }
        }else{
            errorsResponse.setInfo("Некоректные данные");
        }
        validationErrorResponse.setErrors(messages);
        return new ResponseEntity<>(validationErrorResponse,HttpStatus.BAD_REQUEST);
    }
}


