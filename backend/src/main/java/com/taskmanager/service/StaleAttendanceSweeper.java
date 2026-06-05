package com.taskmanager.service;

import com.taskmanager.entity.AttendanceLog;
import com.taskmanager.repository.AttendanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Safety net for missed clock-outs. The Redis active entry self-expires after 16h via TTL;
 * this sweeper flags the corresponding open attendance row in the database so payroll can
 * review it. Chosen over Redis keyspace-expiry notifications, which are best-effort and not
 * delivery-guaranteed.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StaleAttendanceSweeper {

    private final AttendanceLogRepository attendanceRepository;

    @Scheduled(fixedDelayString = "${app.attendance.sweeper-interval-ms:900000}")
    @Transactional
    public void flagStaleOpenAttendance() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(16);
        List<AttendanceLog> stale =
                attendanceRepository.findByClockOutTimeIsNullAndClockInTimeBeforeAndFlaggedFalse(cutoff);
        if (stale.isEmpty()) {
            return;
        }
        stale.forEach(a -> a.setFlagged(true));
        attendanceRepository.saveAll(stale);
        log.warn("Flagged {} attendance record(s) with a missed clock-out (open > 16h)", stale.size());
    }
}
