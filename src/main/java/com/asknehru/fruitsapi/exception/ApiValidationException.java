package com.asknehru.fruitsapi.exception;

import java.util.List;
import java.util.Map;

public class ApiValidationException extends RuntimeException {

    private final Map<String, List<String>> errors;

    public ApiValidationException(Map<String, List<String>> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
