package com.taskmanager.repository;

import com.taskmanager.entity.OvertimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, UUID> {

    /** All overtime entries for a worker within a date range (used for monthly summary + settlement). */
    List<OvertimeEntry> findByWorker_IdAndEntryDateBetween(UUID workerId, LocalDate from, LocalDate to);

    boolean existsByAttendance_Id(UUID attendanceId);
}
