package com.taskmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ClockInRequest {

    @NotNull(message = "workerId is required")
    private UUID workerId;

    @NotNull(message = "siteId is required")
    private UUID siteId;

    /** Optional; defaults to server time. Allows after-the-fact logging/backfill. Must not be in the future. */
    private LocalDateTime clockInTime;
}
