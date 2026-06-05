package com.taskmanager.dto.request;

import com.taskmanager.enums.Designation;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/** All fields optional; only non-null fields are applied (partial update). */
@Data
public class UpdateWorkerRequest {

    private String name;

    private Designation designation;

    @DecimalMin(value = "0.0", inclusive = true, message = "Daily wage rate must be >= 0")
    private BigDecimal dailyWageRate;

    private Boolean active;
}
