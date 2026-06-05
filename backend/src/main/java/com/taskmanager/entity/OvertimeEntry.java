package com.taskmanager.entity;

import com.taskmanager.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "overtime_entries",
        uniqueConstraints = @UniqueConstraint(name = "uq_overtime_attendance", columnNames = "attendance_id"),
        indexes = {
                @Index(name = "idx_overtime_worker_date", columnList = "worker_id, entry_date"),
                @Index(name = "idx_overtime_worker_status", columnList = "worker_id, settlement_status")
        })
@Check(name = "chk_overtime_amounts_non_negative", constraints = "overtime_hours >= 0 and amount >= 0")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OvertimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceLog attendance;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "overtime_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal overtimeHours;

    /**
     * Base hourly wage rate (dailyWageRate / 8) snapshotted at clock-out time and used
     * as the multiplicand for the tiered OT multipliers (1.5x / 2x). Snapshotting freezes
     * historical payouts so a later wage-rate change cannot rewrite past overtime amounts.
     */
    @Column(name = "overtime_rate_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeRateApplied;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    private SettlementStatus settlementStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
