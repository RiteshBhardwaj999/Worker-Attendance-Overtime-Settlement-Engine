package com.taskmanager.dto.response;

import java.math.BigDecimal;

/** Shape of the external (government) minimum-wage API response. */
public record MinimumWageResponse(BigDecimal dailyMinimumWage) {
}
