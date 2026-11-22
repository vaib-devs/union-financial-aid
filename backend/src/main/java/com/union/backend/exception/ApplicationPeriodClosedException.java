package com.union.backend.exception;

public class ApplicationPeriodClosedException extends RuntimeException{
    public ApplicationPeriodClosedException(String message){
        super(message);
    }

}
