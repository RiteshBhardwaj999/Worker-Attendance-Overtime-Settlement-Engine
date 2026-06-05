package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_logs", indexes = {
        @Index(name = "idx_attendance_worker", columnList = "worker_id"),
        @Index(name = "idx_attendance_worker_clockin", columnList = "worker_id, clock_in_time"),
        @Index(name = "idx_attendance_open", columnList = "worker_id, clock_out_time")
})
@Check(name = "chk_attendance_hours_non_negative",
        constraints = "(total_hours is null or total_hours >= 0) and (overtime_hours is null or overtime_hours >= 0)")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "clock_in_time", nullable = false)
    private LocalDateTime clockInTime;

    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;

    @Column(name = "total_hours", precision = 6, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "overtime_hours", precision = 6, scale = 2)
    private BigDecimal overtimeHours;

    @Column(nullable = false)
    private boolean flagged;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
