package com.taskmanager.dto.request;

import com.taskmanager.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Task title is required")
    private String title;
    private String description;
    private TaskStatus status;
    private UUID assigneeId;
    private LocalDateTime dueDate;
}
