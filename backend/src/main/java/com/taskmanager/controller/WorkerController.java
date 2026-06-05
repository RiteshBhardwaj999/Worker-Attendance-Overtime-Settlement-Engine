package com.taskmanager.controller;

import com.taskmanager.dto.request.CreateWorkerRequest;
import com.taskmanager.dto.request.UpdateWorkerRequest;
import com.taskmanager.dto.response.WorkerResponse;
import com.taskmanager.service.WorkerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    @PostMapping
    public ResponseEntity<WorkerResponse> create(@Valid @RequestBody CreateWorkerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workerService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<WorkerResponse>> list() {
        return ResponseEntity.ok(workerService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(workerService.get(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkerResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateWorkerRequest req) {
        return ResponseEntity.ok(workerService.update(id, req));
    }
}
