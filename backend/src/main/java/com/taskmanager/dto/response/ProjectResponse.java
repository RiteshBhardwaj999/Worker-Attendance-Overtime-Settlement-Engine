package com.taskmanager.dto.response;

import com.taskmanager.entity.Project;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private String ownerName;
    private LocalDateTime createdAt;

    public static ProjectResponse from(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.description = p.getDescription();
        r.ownerId = p.getOwner().getId();
        r.ownerName = p.getOwner().getName();
        r.createdAt = p.getCreatedAt();
        return r;
    }
}
