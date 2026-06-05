package com.taskmanager.event;

import com.taskmanager.entity.Worker;

import java.math.BigDecimal;

public record OvertimeSettledEvent(Worker worker, String month, BigDecimal totalAmount, int entriesSettled) {
}
