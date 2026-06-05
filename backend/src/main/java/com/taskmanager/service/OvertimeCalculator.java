package com.taskmanager.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure overtime math, isolated so it is trivially unit-testable.
 *
 * Rules:
 *  - Standard shift = 8h. Anything beyond 8h in one attendance = overtime.
 *  - Tiered rate on payable OT hours: first 2h at 1.5x, beyond at 2x, of the hourly rate.
 *  - Hourly rate = dailyWageRate / 8.
 *  - Monthly cap of 60 OT hours per worker is enforced by the caller before calling {@link #amount}.
 */
@Component
public class OvertimeCalculator {

    public static final BigDecimal STANDARD_SHIFT_HOURS = new BigDecimal("8");
    public static final BigDecimal MONTHLY_CAP_HOURS = new BigDecimal("60");
    public static final BigDecimal FLAG_THRESHOLD_HOURS = new BigDecimal("16");

    private static final BigDecimal HOURS_PER_DAY = new BigDecimal("8");
    private static final BigDecimal TIER1_LIMIT = new BigDecimal("2");
    private static final BigDecimal TIER1_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal TIER2_MULTIPLIER = new BigDecimal("2");

    /** Hourly base rate the OT multipliers apply to. */
    public BigDecimal hourlyRate(BigDecimal dailyWageRate) {
        return dailyWageRate.divide(HOURS_PER_DAY, 2, RoundingMode.HALF_UP);
    }

    /** Overtime hours actually worked = hours beyond the 8h standard shift (never negative). */
    public BigDecimal rawOvertime(BigDecimal totalHours) {
        BigDecimal ot = totalHours.subtract(STANDARD_SHIFT_HOURS);
        return ot.signum() > 0 ? ot : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    /** Tiered payout for the given payable OT hours: first 2h @1.5x, remainder @2x. */
    public BigDecimal amount(BigDecimal payableOtHours, BigDecimal hourlyRate) {
        BigDecimal tier1 = payableOtHours.min(TIER1_LIMIT).max(BigDecimal.ZERO);
        BigDecimal tier2 = payableOtHours.subtract(TIER1_LIMIT).max(BigDecimal.ZERO);
        BigDecimal payout = tier1.multiply(hourlyRate).multiply(TIER1_MULTIPLIER)
                .add(tier2.multiply(hourlyRate).multiply(TIER2_MULTIPLIER));
        return payout.setScale(2, RoundingMode.HALF_UP);
    }
}
