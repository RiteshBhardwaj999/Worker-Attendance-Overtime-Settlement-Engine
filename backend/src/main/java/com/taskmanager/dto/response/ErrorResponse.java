package com.taskmanager.dto.response;

import java.time.Instant;

/**
 * Structured error payload returned for every failure:
 * <pre>{ "error": "DUPLICATE_CLOCK_IN", "message": "...", "timestamp": "2026-05-25T10:30:00Z" }</pre>
 */
public record ErrorResponse(String error, String message, Instant timestamp) {

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, Instant.now());
    }
}
