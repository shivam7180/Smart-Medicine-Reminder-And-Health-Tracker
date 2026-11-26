package com.medicare.admin.controller;

import com.medicare.admin.entity.AuditLog;
import com.medicare.admin.service.AdminService;
import com.medicare.admin.service.AuditLogService;
import com.medicare.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Platform Governance", description = "System metrics, audit trail inspection, and governance")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;

    @GetMapping("/overview")
    @Operation(summary = "Get high-level system overview and platform metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getSystemOverview()));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system audit logs with pagination")
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getLogs(PageRequest.of(page, size))));
    }

    @GetMapping("/audit-logs/recent")
    @Operation(summary = "Get 20 most recent system audit events")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getRecentLogs()));
    }

    @PostMapping("/audit-logs")
    @Operation(summary = "Internal API: Record an administrative or security audit event")
    public ResponseEntity<ApiResponse<AuditLog>> recordAuditLog(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String userEmail,
            @RequestParam String action,
            @RequestParam String entityName,
            @RequestParam(required = false) String entityId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String details) {
        AuditLog saved = auditLogService.recordLog(userId, userEmail, action, entityName, entityId, ipAddress, details);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
