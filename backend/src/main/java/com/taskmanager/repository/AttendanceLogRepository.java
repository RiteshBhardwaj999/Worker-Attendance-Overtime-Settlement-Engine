package com.taskmanager.repository;

import com.taskmanager.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {

    /** The worker's currently-open attendance record (clocked in, not yet clocked out). */
    Optional<AttendanceLog> findByWorker_IdAndClockOutTimeIsNull(UUID workerId);

    boolean existsByWorker_IdAndClockOutTimeIsNull(UUID workerId);

    /** All currently-open attendance records (everyone on-site right now). */
    List<AttendanceLog> findByClockOutTimeIsNull();

    /** Open attendance left running past the cutoff and not yet flagged (missed clock-out sweep). */
    List<AttendanceLog> findByClockOutTimeIsNullAndClockInTimeBeforeAndFlaggedFalse(LocalDateTime cutoff);

    /** Attendance history for a worker within a clock-in date range (v1: unpaginated). */
    List<AttendanceLog> findByWorker_IdAndClockInTimeBetween(UUID workerId, LocalDateTime from, LocalDateTime to);
}
