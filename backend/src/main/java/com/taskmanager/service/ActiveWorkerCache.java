package com.taskmanager.service;

import com.taskmanager.dto.response.ActiveWorkerResponse;
import com.taskmanager.entity.AttendanceLog;
import com.taskmanager.entity.Worker;
import lombok.RequiredArgsConstructor;
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
 * Redis-backed store of workers currently on-site. {@code GET /api/attendance/active}
 * is served exclusively from here, never the database.
 *
 * Each entry carries a 16h TTL safety net: nobody should be clocked in longer than a
 * 16h shift, so a missed clock-out self-expires from the active list. (Stale open
 * attendance rows are separately flagged by StaleAttendanceSweeper.)
 */
@Component
@RequiredArgsConstructor
public class ActiveWorkerCache {

    public static final String KEY_PREFIX = "attendance:active:";
    private static final Duration TTL = Duration.ofHours(16);

    private final RedisTemplate<String, Object> redisTemplate;

    /** Called on clock-in. */
    public void markActive(AttendanceLog log) {
        redisTemplate.opsForValue().set(key(log.getWorker().getId()), ActiveWorkerResponse.from(log), TTL);
    }

    /** Called on clock-out. */
    public void removeActive(UUID workerId) {
        redisTemplate.delete(key(workerId));
    }

    /**
     * Refreshes the cached entry when a worker's profile changes (name/designation),
     * so the active list never serves stale data. Preserves the remaining TTL.
     */
    public void refreshWorker(Worker worker) {
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
    }

    /** Everyone currently on-site, read from Redis via SCAN (non-blocking, unlike KEYS). */
    public List<ActiveWorkerResponse> getActiveWorkers() {
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
    }

    private String key(UUID workerId) {
        return KEY_PREFIX + workerId;
    }
}
