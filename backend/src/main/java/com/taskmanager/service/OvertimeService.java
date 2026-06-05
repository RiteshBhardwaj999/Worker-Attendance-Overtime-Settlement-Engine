package com.taskmanager.service;

import com.taskmanager.dto.response.OvertimeSummaryResponse;
import com.taskmanager.dto.response.OvertimeSummaryResponse.DateBreakdown;
import com.taskmanager.dto.response.SettlementResponse;
import com.taskmanager.entity.OvertimeEntry;
import com.taskmanager.entity.Worker;
import com.taskmanager.enums.SettlementStatus;
import com.taskmanager.exception.ApiException;
import com.taskmanager.exception.ErrorCode;
import com.taskmanager.repository.OvertimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OvertimeService {

    private final OvertimeEntryRepository overtimeRepository;
    private final WorkerService workerService;
    private final MinimumWageClient minimumWageClient;
    private final SmsService smsService;

    @Transactional(readOnly = true)
    public OvertimeSummaryResponse getSummary(UUID workerId, YearMonth month) {
        Worker worker = workerService.getWorkerOrThrow(workerId);

        // v1: external call sits INSIDE the transaction, holding a DB connection (fixed in LF-205).
        BigDecimal minimumDailyWage = minimumWageClient.getDailyMinimumWage();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<OvertimeEntry> entries = overtimeRepository.findByWorker_IdAndEntryDateBetween(workerId, start, end);

        List<DateBreakdown> breakdown = entries.stream()
                .sorted(Comparator.comparing(OvertimeEntry::getEntryDate))
                .map(e -> new DateBreakdown(e.getEntryDate(), e.getOvertimeHours(), e.getAmount(), e.getSettlementStatus()))
                .toList();

        BigDecimal totalHours = entries.stream()
                .map(OvertimeEntry::getOvertimeHours).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPayout = entries.stream()
                .map(OvertimeEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return OvertimeSummaryResponse.builder()
                .workerId(workerId)
                .workerName(worker.getName())
                .month(month.toString())
                .totalOvertimeHours(totalHours)
                .totalPayout(totalPayout)
                .settlementStatus(deriveOverallStatus(entries))
                .minimumDailyWageReference(minimumDailyWage)
                .breakdown(breakdown)
                .build();
    }

    /**
     * v1: NOT annotated @Transactional, so each save() commits on its own — a failure
     * mid-loop leaves a partially-settled month. The SMS is also fired inline (before the
     * data is durably committed). Both are fixed in LF-204.
     */
    public SettlementResponse settle(UUID workerId, YearMonth month) {
        Worker worker = workerService.getWorkerOrThrow(workerId);

        if (!month.isBefore(YearMonth.now())) {
            throw new ApiException(ErrorCode.CANNOT_SETTLE_CURRENT_MONTH,
                    "Cannot settle the current or a future month; only completed months can be settled");
        }

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<OvertimeEntry> all = overtimeRepository.findByWorker_IdAndEntryDateBetween(workerId, start, end);
        if (all.isEmpty()) {
            throw new ApiException(ErrorCode.NOTHING_TO_SETTLE, "No overtime entries to settle for " + month);
        }
        List<OvertimeEntry> pending = all.stream()
                .filter(e -> e.getSettlementStatus() == SettlementStatus.PENDING).toList();
        if (pending.isEmpty()) {
            throw new ApiException(ErrorCode.ALREADY_SETTLED, "Overtime for " + month + " is already settled");
        }

        BigDecimal total = pending.stream().map(OvertimeEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        for (OvertimeEntry entry : pending) {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            overtimeRepository.save(entry);
        }

        smsService.send(worker, "Your " + month + " overtime of Rs " + total + " has been settled.");

        return new SettlementResponse(workerId, month.toString(), total, pending.size(), SettlementStatus.SETTLED);
    }

    private SettlementStatus deriveOverallStatus(List<OvertimeEntry> entries) {
        if (entries.isEmpty()) {
            return SettlementStatus.PENDING;
        }
        boolean allSettled = entries.stream().allMatch(e -> e.getSettlementStatus() == SettlementStatus.SETTLED);
        return allSettled ? SettlementStatus.SETTLED : SettlementStatus.PENDING;
    }
}
