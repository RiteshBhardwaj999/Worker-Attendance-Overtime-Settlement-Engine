package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClockOutRequest {

    @NotNull(message = "workerId is required")
    private UUID workerId;

    /** Optional; defaults to server time. Must not be in the future and must be after clock-in. */
    private LocalDateTime clockOutTime;
}
