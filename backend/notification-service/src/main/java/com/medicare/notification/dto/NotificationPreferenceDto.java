package com.medicare.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDto {
    private Long id;
    private Long userId;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Integer reminderLeadMinutes;
    private Boolean lowStockAlerts;
}
