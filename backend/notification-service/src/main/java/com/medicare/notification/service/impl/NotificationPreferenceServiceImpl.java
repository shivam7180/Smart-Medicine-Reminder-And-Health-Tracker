package com.medicare.notification.service.impl;

import com.medicare.notification.dto.NotificationPreferenceDto;
import com.medicare.notification.entity.NotificationPreference;
import com.medicare.notification.repository.NotificationPreferenceRepository;
import com.medicare.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Override
    @Transactional
    public NotificationPreferenceDto getPreferences(Long userId) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> preferenceRepository.save(NotificationPreference.builder()
                        .userId(userId)
                        .emailEnabled(true)
                        .inAppEnabled(true)
                        .reminderLeadMinutes(15)
                        .lowStockAlerts(true)
                        .build()));

        return mapToDto(pref);
    }

    @Override
    @Transactional
    public NotificationPreferenceDto updatePreferences(Long userId, NotificationPreferenceDto dto) {
        NotificationPreference pref = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.builder().userId(userId).build());

        if (dto.getEmailEnabled() != null) pref.setEmailEnabled(dto.getEmailEnabled());
        if (dto.getInAppEnabled() != null) pref.setInAppEnabled(dto.getInAppEnabled());
        if (dto.getReminderLeadMinutes() != null) pref.setReminderLeadMinutes(dto.getReminderLeadMinutes());
        if (dto.getLowStockAlerts() != null) pref.setLowStockAlerts(dto.getLowStockAlerts());

        return mapToDto(preferenceRepository.save(pref));
    }

    private NotificationPreferenceDto mapToDto(NotificationPreference p) {
        return NotificationPreferenceDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .emailEnabled(p.getEmailEnabled())
                .inAppEnabled(p.getInAppEnabled())
                .reminderLeadMinutes(p.getReminderLeadMinutes())
                .lowStockAlerts(p.getLowStockAlerts())
                .build();
    }
}
