package com.taskmanager.service;

import com.taskmanager.entity.Worker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock SMS gateway. In production this would call an SMS provider; here it logs.
 * Notifications are a side effect and must never run inside a DB transaction.
 */
@Service
@Slf4j
public class SmsService {

    public void send(Worker worker, String message) {
        log.info("[SMS] -> {} ({}): {}", worker.getName(), worker.getPhone(), message);
    }
}
