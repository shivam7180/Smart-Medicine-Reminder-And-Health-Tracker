package com.medicare.notification.service.impl;

import com.medicare.common.exception.ResourceNotFoundException;
import com.medicare.notification.dto.CreateNotificationRequest;
import com.medicare.notification.dto.NotificationDto;
import com.medicare.notification.entity.Notification;
import com.medicare.notification.entity.NotificationDeliveryLog;
import com.medicare.notification.repository.NotificationDeliveryLogRepository;
import com.medicare.notification.repository.NotificationPreferenceRepository;
import com.medicare.notification.repository.NotificationRepository;
import com.medicare.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationDeliveryLogRepository deliveryLogRepository;

    @Override
    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .notificationType(request.getNotificationType().trim())
                .referenceId(request.getReferenceId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Record in-app delivery log
        NotificationDeliveryLog inAppLog = NotificationDeliveryLog.builder()
                .notificationId(saved.getId())
                .channel("IN_APP")
                .status("SUCCESS")
                .attemptCount(1)
                .deliveredAt(LocalDateTime.now())
                .build();
        deliveryLogRepository.save(inAppLog);

        log.info("Notification {} created for user {}", saved.getId(), saved.getUserId());
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notif = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notif.setIsRead(true);
        notif.setReadAt(LocalDateTime.now());
        notificationRepository.save(notif);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            n.setIsRead(true);
            n.setReadAt(now);
        }
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notif = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notificationRepository.delete(notif);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
