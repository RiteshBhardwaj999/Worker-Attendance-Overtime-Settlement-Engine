package com.taskmanager.dto.response;

import com.taskmanager.entity.AttendanceLog;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceResponse {
    private UUID id;
    private UUID workerId;
    private String workerName;
    private UUID siteId;
    private String siteName;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private boolean flagged;

    public static AttendanceResponse from(AttendanceLog log) {
        AttendanceResponse r = new AttendanceResponse();
        r.id = log.getId();
        r.workerId = log.getWorker().getId();
        r.workerName = log.getWorker().getName();
        r.siteId = log.getSite().getId();
        r.siteName = log.getSite().getSiteName();
        r.clockInTime = log.getClockInTime();
        r.clockOutTime = log.getClockOutTime();
        r.totalHours = log.getTotalHours();
        r.overtimeHours = log.getOvertimeHours();
        r.flagged = log.isFlagged();
        return r;
    }
}
