package com.taskmanager.controller;

import com.taskmanager.dto.request.CreateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskRequest;
import com.taskmanager.dto.request.UpdateTaskStatusRequest;
import com.taskmanager.dto.response.TaskResponse;
import com.taskmanager.service.TaskService;
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
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(@AuthenticationPrincipal UserDetails ud,
                                                    @PathVariable UUID projectId) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(taskService.listTasks(userId, projectId));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal UserDetails ud,
                                                @PathVariable UUID projectId,
                                                @Valid @RequestBody CreateTaskRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(userId, projectId, req));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> get(@AuthenticationPrincipal UserDetails ud,
                                             @PathVariable UUID projectId,
                                             @PathVariable UUID taskId) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(taskService.getTask(userId, projectId, taskId));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> update(@AuthenticationPrincipal UserDetails ud,
                                                @PathVariable UUID projectId,
                                                @PathVariable UUID taskId,
                                                @RequestBody UpdateTaskRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(taskService.updateTask(userId, projectId, taskId, req));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateStatus(@AuthenticationPrincipal UserDetails ud,
                                                      @PathVariable UUID projectId,
                                                      @PathVariable UUID taskId,
                                                      @Valid @RequestBody UpdateTaskStatusRequest req) {
        UUID userId = UUID.fromString(ud.getUsername());
        return ResponseEntity.ok(taskService.updateTaskStatus(userId, projectId, taskId, req));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails ud,
                                        @PathVariable UUID projectId,
                                        @PathVariable UUID taskId) {
        UUID userId = UUID.fromString(ud.getUsername());
        taskService.deleteTask(userId, projectId, taskId);
        return ResponseEntity.noContent().build();
    }
}
