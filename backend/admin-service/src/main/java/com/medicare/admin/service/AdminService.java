package com.medicare.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuditLogService auditLogService;

    public Map<String, Object> getSystemOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("platformVersion", "v1.0.0-PROD");
        stats.put("status", "HEALTHY");
        stats.put("activeMicroservices", 8);
        stats.put("uptimeSince", LocalDateTime.now().minusDays(14));
        stats.put("recentAuditCount", auditLogService.getRecentLogs().size());
        stats.put("databaseConnected", true);
        return stats;
    }
}
