package com.taskmanager.controller;

import com.taskmanager.dto.request.CreateProjectRequest;
import com.taskmanager.dto.response.ProjectResponse;
import com.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@AuthenticationPrincipal UserDetails ud,
                                                   @Valid @RequestBody CreateProjectRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(userId, req));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> list(@AuthenticationPrincipal UserDetails ud) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(projectService.getUserProjects(userId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> get(@AuthenticationPrincipal UserDetails ud,
                                                @PathVariable UUID projectId) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(projectService.getProjectById(userId, projectId));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> update(@AuthenticationPrincipal UserDetails ud,
                                                   @PathVariable UUID projectId,
                                                   @RequestBody CreateProjectRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(projectService.updateProject(userId, projectId, req));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,
                                        @PathVariable UUID projectId) {
        UUID userId = UUID.fromString(ud.getUsername());
        projectService.deleteProject(userId, projectId);
        return ResponseEntity.noContent().build();
    }
}
