package com.taskmanager.controller;

import com.taskmanager.dto.request.CreateSiteRequest;
import com.taskmanager.dto.response.SiteResponse;
import com.taskmanager.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody CreateSiteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(siteService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<SiteResponse>> list() {
        return ResponseEntity.ok(siteService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(siteService.get(id));
    }
}
