package com.taskmanager.controller;

import com.taskmanager.dto.response.OvertimeSummaryResponse;
import com.taskmanager.dto.response.SettlementResponse;
import com.taskmanager.exception.ApiException;
import com.taskmanager.exception.ErrorCode;
import com.taskmanager.service.OvertimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequestMapping("/api/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @GetMapping("/summary/{workerId}")
    public ResponseEntity<OvertimeSummaryResponse> summary(@PathVariable UUID workerId,
                                                           @RequestParam String month) {
        return ResponseEntity.ok(overtimeService.getSummary(workerId, parseMonth(month)));
    }

    @PostMapping("/settle/{workerId}")
    public ResponseEntity<SettlementResponse> settle(@PathVariable UUID workerId,
                                                     @RequestParam String month) {
        return ResponseEntity.ok(overtimeService.settle(workerId, parseMonth(month)));
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid month '" + month + "'; expected format YYYY-MM");
        }
    }
}
