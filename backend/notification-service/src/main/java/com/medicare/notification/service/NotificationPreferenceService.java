package com.medicare.notification.service;

import com.medicare.notification.dto.NotificationPreferenceDto;

public interface NotificationPreferenceService {
    NotificationPreferenceDto getPreferences(Long userId);
    NotificationPreferenceDto updatePreferences(Long userId, NotificationPreferenceDto dto);
}
