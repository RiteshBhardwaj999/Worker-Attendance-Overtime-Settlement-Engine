package com.taskmanager.service;

import com.taskmanager.dto.response.ActiveWorkerResponse;
import com.taskmanager.entity.AttendanceLog;
import com.taskmanager.entity.Worker;
import com.taskmanager.repository.AttendanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed store of workers currently on-site. Every Redis operation is wrapped
 * in try/catch so a Redis outage never crashes the application (LF-202).
 *
 * {@code getActiveWorkers()} falls back to the database when Redis is unavailable,
 * ensuring the /active endpoint always returns a useful response.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActiveWorkerCache {

    public static final String KEY_PREFIX = "attendance:active:";
    private static final Duration TTL = Duration.ofHours(16);

    private final RedisTemplate<String, Object> redisTemplate;
    private final AttendanceLogRepository attendanceRepository;

    public void markActive(AttendanceLog attendanceLog) {
        try {
            redisTemplate.opsForValue().set(key(attendanceLog.getWorker().getId()), ActiveWorkerResponse.from(attendanceLog), TTL);
        } catch (Exception ex) {
            log.warn("Redis markActive failed for worker {}: {}", attendanceLog.getWorker().getId(), ex.getMessage());
        }
    }

    public void removeActive(UUID workerId) {
        try {
            redisTemplate.delete(key(workerId));
        } catch (Exception ex) {
            log.warn("Redis removeActive failed for worker {}: {}", workerId, ex.getMessage());
        }
    }

    public void refreshWorker(Worker worker) {
        try {
            String key = key(worker.getId());
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof ActiveWorkerResponse active) {
                active.setWorkerName(worker.getName());
                active.setDesignation(worker.getDesignation());
                Long remaining = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (remaining != null && remaining > 0) {
                    redisTemplate.opsForValue().set(key, active, remaining, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, active, TTL);
                }
            }
        } catch (Exception ex) {
            log.warn("Redis refreshWorker failed for worker {}: {}", worker.getId(), ex.getMessage());
        }
    }

    /**
     * Returns workers currently clocked in. Tries Redis first (fast SCAN); if Redis
     * is unavailable, falls back to the database so the endpoint stays functional.
     */
    public List<ActiveWorkerResponse> getActiveWorkers() {
        try {
            List<ActiveWorkerResponse> result = new ArrayList<>();
            ScanOptions options = ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(100).build();
            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    Object value = redisTemplate.opsForValue().get(cursor.next());
                    if (value instanceof ActiveWorkerResponse active) {
                        result.add(active);
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("Redis getActiveWorkers failed, falling back to DB: {}", ex.getMessage());
            return attendanceRepository.findByClockOutTimeIsNull().stream()
                    .map(ActiveWorkerResponse::from)
                    .toList();
        }
    }

    private String key(UUID workerId) {
        return KEY_PREFIX + workerId;
    }
}
