package com.taskmanager.dto.response;

import com.taskmanager.enums.SettlementStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementResponse(
        UUID workerId,
        String month,
        BigDecimal totalAmount,
        int entriesSettled,
        SettlementStatus status) {
}
