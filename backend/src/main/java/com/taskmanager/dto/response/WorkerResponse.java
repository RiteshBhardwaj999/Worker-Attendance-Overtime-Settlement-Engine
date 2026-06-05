package com.taskmanager.dto.response;

import com.taskmanager.entity.Worker;
import com.taskmanager.enums.Designation;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WorkerResponse {
    private UUID id;
    private String name;
    private String phone;
    private Designation designation;
    private BigDecimal dailyWageRate;
    private boolean active;
    private LocalDateTime createdAt;

    public static WorkerResponse from(Worker w) {
        WorkerResponse r = new WorkerResponse();
        r.id = w.getId();
        r.name = w.getName();
        r.phone = w.getPhone();
        r.designation = w.getDesignation();
        r.dailyWageRate = w.getDailyWageRate();
        r.active = w.isActive();
        r.createdAt = w.getCreatedAt();
        return r;
    }
}
