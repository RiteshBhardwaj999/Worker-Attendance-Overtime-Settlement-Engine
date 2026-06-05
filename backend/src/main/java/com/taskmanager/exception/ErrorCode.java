package com.taskmanager.exception;

import org.springframework.http.HttpStatus;

/**
 * Stable, machine-readable error codes returned in the {@code error} field of every
 * structured error response, each mapped to the HTTP status it should produce.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),

    WORKER_NOT_FOUND(HttpStatus.NOT_FOUND),
    SITE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND),

    WORKER_INACTIVE(HttpStatus.BAD_REQUEST),
    SITE_INACTIVE(HttpStatus.BAD_REQUEST),
    FUTURE_CLOCK_IN(HttpStatus.BAD_REQUEST),

    DUPLICATE_PHONE(HttpStatus.CONFLICT),
    DUPLICATE_CLOCK_IN(HttpStatus.CONFLICT),
    NOT_CLOCKED_IN(HttpStatus.CONFLICT),

    CANNOT_SETTLE_CURRENT_MONTH(HttpStatus.BAD_REQUEST),
    NOTHING_TO_SETTLE(HttpStatus.NOT_FOUND),
    ALREADY_SETTLED(HttpStatus.CONFLICT),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
