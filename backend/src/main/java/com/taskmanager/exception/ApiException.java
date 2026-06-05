package com.taskmanager.exception;

import lombok.Getter;

/**
 * Domain exception carrying a machine-readable {@link ErrorCode}. The
 * GlobalExceptionHandler turns this into a structured JSON error with the
 * correct HTTP status.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
