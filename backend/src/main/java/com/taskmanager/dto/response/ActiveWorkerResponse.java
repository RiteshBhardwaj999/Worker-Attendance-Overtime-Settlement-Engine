package com.taskmanager.dto.response;

import com.taskmanager.entity.AttendanceLog;
import com.taskmanager.enums.Designation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A worker currently on-site (clocked in, not yet out). Served from Redis in the cache layer. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveWorkerResponse {
    private UUID workerId;
    private String workerName;
    private Designation designation;
    private UUID siteId;
    private String siteName;
    private LocalDateTime clockInTime;

    public static ActiveWorkerResponse from(AttendanceLog log) {
        return new ActiveWorkerResponse(
                log.getWorker().getId(),
                log.getWorker().getName(),
                log.getWorker().getDesignation(),
                log.getSite().getId(),
                log.getSite().getSiteName(),
                log.getClockInTime());
    }
}
