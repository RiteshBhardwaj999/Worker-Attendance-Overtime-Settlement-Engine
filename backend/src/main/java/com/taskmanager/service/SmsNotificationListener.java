package com.taskmanager.service;

import com.taskmanager.event.OvertimeSettledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fires the SMS only after the settlement transaction has committed (LF-204).
 * Using AFTER_COMMIT guarantees the worker never receives a notification for a
 * settlement that was rolled back.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmsNotificationListener {

    private final SmsService smsService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOvertimeSettled(OvertimeSettledEvent event) {
        smsService.send(event.worker(),
                "Your " + event.month() + " overtime of Rs " + event.totalAmount() + " has been settled.");
    }
}
