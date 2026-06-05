package com.taskmanager.dto.response;

import com.taskmanager.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeSummaryResponse {
    private UUID workerId;
    private String workerName;
    private String month;
    private BigDecimal totalOvertimeHours;
    private BigDecimal totalPayout;
    private SettlementStatus settlementStatus;
    private BigDecimal minimumDailyWageReference;
    private List<DateBreakdown> breakdown;

    @Data
    @AllArgsConstructor
    public static class DateBreakdown {
        private LocalDate date;
        private BigDecimal overtimeHours;
        private BigDecimal amount;
        private SettlementStatus settlementStatus;
    }
}
