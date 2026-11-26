package com.medicare.notification.service;

import com.medicare.notification.dto.CreateNotificationRequest;
import com.medicare.notification.dto.NotificationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationDto createNotification(CreateNotificationRequest request);
    Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable);
    List<NotificationDto> getUnreadNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long userId, Long notificationId);
}
