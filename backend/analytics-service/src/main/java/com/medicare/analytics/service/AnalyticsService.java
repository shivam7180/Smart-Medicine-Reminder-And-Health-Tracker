package com.medicare.analytics.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Object> getPatientDashboardSummary(Long patientId);
    Map<String, Object> getComprehensiveAdherenceReport(Long patientId, int days);
}
