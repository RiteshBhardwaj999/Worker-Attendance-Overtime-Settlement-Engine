package com.taskmanager.service;

import com.taskmanager.dto.request.CreateWorkerRequest;
import com.taskmanager.dto.request.UpdateWorkerRequest;
import com.taskmanager.dto.response.WorkerResponse;
import com.taskmanager.entity.Worker;
import com.taskmanager.exception.ApiException;
import com.taskmanager.exception.ErrorCode;
import com.taskmanager.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final ActiveWorkerCache activeWorkerCache;

    @Transactional
    public WorkerResponse create(CreateWorkerRequest req) {
        if (workerRepository.existsByPhone(req.getPhone())) {
            throw new ApiException(ErrorCode.DUPLICATE_PHONE,
                    "A worker with phone " + req.getPhone() + " already exists");
        }
        Worker worker = Worker.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .designation(req.getDesignation())
                .dailyWageRate(req.getDailyWageRate())
                .active(true)
                .build();
        return WorkerResponse.from(workerRepository.save(worker));
    }

    public List<WorkerResponse> list() {
        return workerRepository.findAll().stream().map(WorkerResponse::from).toList();
    }

    public WorkerResponse get(UUID id) {
        return WorkerResponse.from(getWorkerOrThrow(id));
    }

    @Transactional
    public WorkerResponse update(UUID id, UpdateWorkerRequest req) {
        Worker worker = getWorkerOrThrow(id);
        if (req.getName() != null && !req.getName().isBlank()) worker.setName(req.getName());
        if (req.getDesignation() != null) worker.setDesignation(req.getDesignation());
        if (req.getDailyWageRate() != null) worker.setDailyWageRate(req.getDailyWageRate());
        if (req.getActive() != null) worker.setActive(req.getActive());
        Worker saved = workerRepository.save(worker);
        // Invalidate/refresh any cached active entry so /active never serves stale name/designation.
        activeWorkerCache.refreshWorker(saved);
        return WorkerResponse.from(saved);
    }

    /** Shared lookup used by attendance/overtime services. */
    public Worker getWorkerOrThrow(UUID id) {
        return workerRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.WORKER_NOT_FOUND, "Worker not found: " + id));
    }
}
