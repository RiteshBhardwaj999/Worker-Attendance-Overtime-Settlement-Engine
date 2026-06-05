package com.taskmanager.repository;

import com.taskmanager.entity.AttendanceLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {

    @EntityGraph(attributePaths = {"worker", "site"})
    Optional<AttendanceLog> findByWorker_IdAndClockOutTimeIsNull(UUID workerId);

    boolean existsByWorker_IdAndClockOutTimeIsNull(UUID workerId);

    @EntityGraph(attributePaths = {"worker", "site"})
    List<AttendanceLog> findByClockOutTimeIsNull();

    List<AttendanceLog> findByClockOutTimeIsNullAndClockInTimeBeforeAndFlaggedFalse(LocalDateTime cutoff);

    /**
     * Paginated, join-fetched log for a single worker.
     * The COUNT query is separate to avoid the fetch join in count queries (Hibernate warning).
     */
    @Query(value = "SELECT a FROM AttendanceLog a JOIN FETCH a.worker JOIN FETCH a.site " +
                   "WHERE a.worker.id = :workerId AND a.clockInTime BETWEEN :from AND :to " +
                   "ORDER BY a.clockInTime DESC",
           countQuery = "SELECT COUNT(a) FROM AttendanceLog a WHERE a.worker.id = :workerId " +
                        "AND a.clockInTime BETWEEN :from AND :to")
    Page<AttendanceLog> findLogByWorkerPaged(
            @Param("workerId") UUID workerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    /** Paginated full log (no worker filter). */
    @Query(value = "SELECT a FROM AttendanceLog a JOIN FETCH a.worker JOIN FETCH a.site ORDER BY a.clockInTime DESC",
           countQuery = "SELECT COUNT(a) FROM AttendanceLog a")
    Page<AttendanceLog> findAllLogPaged(Pageable pageable);
}
