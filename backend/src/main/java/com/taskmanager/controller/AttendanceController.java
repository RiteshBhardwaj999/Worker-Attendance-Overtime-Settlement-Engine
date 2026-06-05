package com.taskmanager.controller;

import com.taskmanager.dto.request.ClockInRequest;
import com.taskmanager.dto.request.ClockOutRequest;
import com.taskmanager.dto.response.ActiveWorkerResponse;
import com.taskmanager.dto.response.AttendanceResponse;
import com.taskmanager.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody ClockInRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.clockIn(req));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut(@Valid @RequestBody ClockOutRequest req) {
        return ResponseEntity.ok(attendanceService.clockOut(req));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ActiveWorkerResponse>> active() {
        return ResponseEntity.ok(attendanceService.getActiveWorkers());
    }

    @GetMapping("/log")
    public ResponseEntity<List<AttendanceResponse>> log(
            @RequestParam(required = false) UUID workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getLog(workerId, from, to));
    }
}
