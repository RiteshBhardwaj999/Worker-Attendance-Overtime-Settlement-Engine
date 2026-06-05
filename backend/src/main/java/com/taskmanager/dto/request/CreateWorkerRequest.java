package com.taskmanager.dto.request;

import com.taskmanager.enums.Designation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWorkerRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Designation is required")
    private Designation designation;

    @NotNull(message = "Daily wage rate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Daily wage rate must be >= 0")
    private BigDecimal dailyWageRate;
}
