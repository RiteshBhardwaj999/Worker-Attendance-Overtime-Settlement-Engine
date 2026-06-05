package com.taskmanager.dto.response;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private UUID projectId;
    private UUID assigneeId;
    private String assigneeName;
    private UUID creatorId;
    private String creatorName;
    private LocalDateTime createdAt;
    private boolean overdue;

    public static TaskResponse from(Task t) {
        TaskResponse r = new TaskResponse();
        r.id = t.getId();
        r.title = t.getTitle();
        r.description = t.getDescription();
        r.status = t.getStatus();
        r.dueDate = t.getDueDate();
        r.projectId = t.getProject().getId();
        r.creatorId = t.getCreator().getId();
        r.creatorName = t.getCreator().getName();
        r.createdAt = t.getCreatedAt();
        if (t.getAssignee() != null) {
            r.assigneeId = t.getAssignee().getId();
            r.assigneeName = t.getAssignee().getName();
        }
        r.overdue = t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDateTime.now())
                && t.getStatus() != TaskStatus.DONE;
        return r;
    }
}
