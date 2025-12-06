// controller/HealthLogController.java
package com.example.viegymapp.controller;

import com.example.viegymapp.dto.request.HealthLogRequest;
import com.example.viegymapp.dto.response.ApiResponse;
import com.example.viegymapp.dto.response.HealthLogResponse;
import com.example.viegymapp.service.HealthLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/health")
public class HealthLogController {

    @Autowired
    private HealthLogService healthLogService;

    // --- Lấy tất cả health log của user hiện tại ---
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<List<HealthLogResponse>> getHealthLogs() {
        return ApiResponse.<List<HealthLogResponse>>builder()
                .result(healthLogService.getHealthLogsOfCurrentUser())
                .build();
    }

    // --- Tạo health log ---
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ApiResponse<HealthLogResponse> create(@RequestBody HealthLogRequest request) {
        return ApiResponse.<HealthLogResponse>builder()
                .result(healthLogService.createHealthLog(request))
                .build();
    }

    // --- Cập nhật health log ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @healthLogService.isOwner(#id, authentication.name)")
    public ApiResponse<HealthLogResponse> update(@PathVariable UUID id,
                                                 @RequestBody HealthLogRequest request) {
        return ApiResponse.<HealthLogResponse>builder()
                .result(healthLogService.updateHealthLog(id, request))
                .build();
    }

    // --- Xóa health log ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @healthLogService.isOwner(#id, authentication.name)")
    public ApiResponse<UUID> delete(@PathVariable UUID id) {
        healthLogService.deleteHealthLog(id);
        return ApiResponse.<UUID>builder()
                .result(id)
                .build();
    }
}
