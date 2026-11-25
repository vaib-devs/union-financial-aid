package com.union.backend.exception;

import java.util.List;

public class ValidationErrorResponse {
    private List<String> errors;

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
}
