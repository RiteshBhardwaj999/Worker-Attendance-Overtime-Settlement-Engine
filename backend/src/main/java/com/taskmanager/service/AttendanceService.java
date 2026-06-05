package com.taskmanager.service;

import com.taskmanager.dto.request.ClockInRequest;
import com.taskmanager.dto.request.ClockOutRequest;
import com.taskmanager.dto.response.ActiveWorkerResponse;
import com.taskmanager.dto.response.AttendanceResponse;
import com.taskmanager.entity.AttendanceLog;
import com.taskmanager.entity.OvertimeEntry;
import com.taskmanager.entity.Site;
import com.taskmanager.entity.Worker;
import com.taskmanager.enums.SettlementStatus;
import com.taskmanager.exception.ApiException;
import com.taskmanager.exception.ErrorCode;
import com.taskmanager.repository.AttendanceLogRepository;
import com.taskmanager.repository.OvertimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceLogRepository attendanceRepository;
    private final OvertimeEntryRepository overtimeRepository;
    private final WorkerService workerService;
    private final SiteService siteService;
    private final OvertimeCalculator overtimeCalculator;
    private final ActiveWorkerCache activeWorkerCache;

    @Transactional
    public AttendanceResponse clockIn(ClockInRequest req) {
        Worker worker = workerService.getWorkerOrThrow(req.getWorkerId());
        if (!worker.isActive()) {
            throw new ApiException(ErrorCode.WORKER_INACTIVE, "Worker is inactive: " + worker.getName());
        }
        Site site = siteService.getSiteOrThrow(req.getSiteId());
        if (!site.isActive()) {
            throw new ApiException(ErrorCode.SITE_INACTIVE, "Site is inactive: " + site.getSiteName());
        }

        LocalDateTime clockInTime = req.getClockInTime() != null ? req.getClockInTime() : LocalDateTime.now();
        if (clockInTime.isAfter(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.FUTURE_CLOCK_IN, "Clock-in time cannot be in the future");
        }

        attendanceRepository.findByWorker_IdAndClockOutTimeIsNull(worker.getId()).ifPresent(open -> {
            throw new ApiException(ErrorCode.DUPLICATE_CLOCK_IN,
                    "Worker is already clocked in at Site: " + open.getSite().getSiteName());
        });

        AttendanceLog log = AttendanceLog.builder()
                .worker(worker)
                .site(site)
                .clockInTime(clockInTime)
                .flagged(false)
                .build();
        AttendanceLog saved = attendanceRepository.save(log);
        activeWorkerCache.markActive(saved);
        return AttendanceResponse.from(saved);
    }

    @Transactional
    public AttendanceResponse clockOut(ClockOutRequest req) {
        Worker worker = workerService.getWorkerOrThrow(req.getWorkerId());
        AttendanceLog log = attendanceRepository.findByWorker_IdAndClockOutTimeIsNull(worker.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_CLOCKED_IN,
                        "Worker is not currently clocked in"));

        LocalDateTime clockOutTime = req.getClockOutTime() != null ? req.getClockOutTime() : LocalDateTime.now();
        if (clockOutTime.isAfter(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Clock-out time cannot be in the future");
        }
        if (!clockOutTime.isAfter(log.getClockInTime())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Clock-out time must be after clock-in time");
        }

        BigDecimal totalHours = hoursBetween(log.getClockInTime(), clockOutTime);
        BigDecimal rawOvertime = overtimeCalculator.rawOvertime(totalHours);

        log.setClockOutTime(clockOutTime);
        log.setTotalHours(totalHours);
        log.setOvertimeHours(rawOvertime);
        // Auto-flag suspiciously long shifts (likely a missed clock-out) for review.
        log.setFlagged(totalHours.compareTo(OvertimeCalculator.FLAG_THRESHOLD_HOURS) > 0);
        attendanceRepository.save(log);

        if (rawOvertime.signum() > 0) {
            recordOvertime(worker, log, clockOutTime.toLocalDate(), rawOvertime);
        }
        activeWorkerCache.removeActive(worker.getId());
        return AttendanceResponse.from(log);
    }

    /** Applies the monthly 60h cap and persists a (possibly capped) overtime entry. */
    private void recordOvertime(Worker worker, AttendanceLog log, LocalDate otDate, BigDecimal rawOvertime) {
        BigDecimal monthToDate = sumMonthlyOvertime(worker.getId(), otDate);
        BigDecimal remaining = OvertimeCalculator.MONTHLY_CAP_HOURS.subtract(monthToDate).max(BigDecimal.ZERO);
        BigDecimal payable = rawOvertime.min(remaining);
        if (payable.signum() <= 0) {
            return; // monthly cap already reached; attendance still recorded, no payable OT
        }
        BigDecimal hourlyRate = overtimeCalculator.hourlyRate(worker.getDailyWageRate());
        OvertimeEntry entry = OvertimeEntry.builder()
                .worker(worker)
                .attendance(log)
                .entryDate(otDate)
                .overtimeHours(payable)
                .overtimeRateApplied(hourlyRate)
                .amount(overtimeCalculator.amount(payable, hourlyRate))
                .settlementStatus(SettlementStatus.PENDING)
                .build();
        overtimeRepository.save(entry);
    }

    private BigDecimal sumMonthlyOvertime(UUID workerId, LocalDate dateInMonth) {
        LocalDate start = dateInMonth.withDayOfMonth(1);
        LocalDate end = dateInMonth.withDayOfMonth(dateInMonth.lengthOfMonth());
        return overtimeRepository.findByWorker_IdAndEntryDateBetween(workerId, start, end).stream()
                .map(OvertimeEntry::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal hoursBetween(LocalDateTime from, LocalDateTime to) {
        long minutes = Duration.between(from, to).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    // ---- reads ----

    /** Served exclusively from Redis, never the database. */
    public List<ActiveWorkerResponse> getActiveWorkers() {
        return activeWorkerCache.getActiveWorkers();
    }

    /** v1: unpaginated; relations are EAGER so this triggers N+1. Hardened in LF-203. */
    public List<AttendanceResponse> getLog(UUID workerId, LocalDate from, LocalDate to) {
        List<AttendanceLog> logs;
        if (workerId == null) {
            logs = attendanceRepository.findAll();
        } else {
            LocalDateTime fromTs = (from != null ? from : LocalDate.of(2000, 1, 1)).atStartOfDay();
            LocalDateTime toTs = (to != null ? to : LocalDate.now()).atTime(LocalTime.MAX);
            logs = attendanceRepository.findByWorker_IdAndClockInTimeBetween(workerId, fromTs, toTs);
        }
        return logs.stream().map(AttendanceResponse::from).toList();
    }
}
